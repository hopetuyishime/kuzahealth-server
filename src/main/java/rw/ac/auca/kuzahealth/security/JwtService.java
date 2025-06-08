package rw.ac.auca.kuzahealth.security;







import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import rw.ac.auca.kuzahealth.core.user.entity.User;

@Service
@RequiredArgsConstructor
public class JwtService {
    private static final String SECRET_KEY = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";



    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractEmail(String token) {
        return extractClaim(token, claims -> claims.get("email", String.class));
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }


    // public String generateToken(User user) {
    //     Map<String, Object> claims = new HashMap<>();
    //     claims.put("username", user.getUsername());
    //     claims.put("email", user.getEmail());
    //     claims.put("role", user.getRole());
        
    //     return Jwts
    //             .builder()
    //             .setClaims(claims)
    //             .setSubject(user.getEmail())
    //             .setIssuer(user.getUsername())
    //             .setIssuedAt(new Date(System.currentTimeMillis()))
    //             .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 7)) // 7 days validity
    //             .signWith(getSignInKey())
    //             .compact();
    // }

    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", user.getUsername());
        claims.put("email", user.getEmail());
        claims.put("role", user.getRole());
    
        return Jwts.builder()
                .setClaims(claims) 
                .setSubject(user.getEmail()) 
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 7))
                .signWith(getSignInKey())
                .compact();
    }
    
    
//     public boolean isTokenValid(String token, UserDetails userDetails) {
//         final String username = extractUsername(token);
//         // Log the details for debugging
// //        System.out.println("Is username matching? " + username.equals(userDetails.getUsername()));
// //        System.out.println("Extracted username: " + username);
// //        System.out.println("UserDetails username: " + userDetails.getUsername());

//         return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
//     }

public boolean isTokenValid(String token, UserDetails userDetails) {
    final String email = extractEmail(token);
    final String username = extractClaim(token, claims -> claims.get("username", String.class));
    final String role = extractClaim(token, claims -> claims.get("role", String.class));

    // Log the extracted values for debugging
    System.out.println("Email from token: " + email);
    System.out.println("Username from token: " + username);
    System.out.println("Role from token: " + role);

    // Validate email, username, and expiration
    return email.equals(userDetails.getUsername()) 
           && username.equals(userDetails.getUsername()) 
           && !isTokenExpired(token)
           && role != null; // Optionally add role validation
}

    
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // private Claims extractAllClaims(String token) {
    //     return Jwts
    //             .parserBuilder()
    //             .setSigningKey(getSignInKey())
    //             .build()
    //             .parseClaimsJws(token)
    //             .getBody();
    // }
    public Map<String, Object> getTokenClaims(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    
        // Extracting custom claims
        Map<String, Object> extractedClaims = new HashMap<>();
        extractedClaims.put("username", claims.get("username"));
        extractedClaims.put("email", claims.getSubject()); // 'sub' claim
        extractedClaims.put("role", claims.get("role"));
    
        return extractedClaims;
    }
    
    private Claims extractAllClaims(String token) {
        try {
            return Jwts
                    .parserBuilder()
                    .setSigningKey(getSignInKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            // Log or rethrow the exception
            System.err.println("Token has expired: " + e.getMessage());
            throw e; // You can throw a custom exception if needed
        }
    }
    
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}