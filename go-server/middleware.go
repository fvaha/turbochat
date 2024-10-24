package main

import (
	"context"
	"net/http"

	"github.com/golang-jwt/jwt/v4"
)

var jwtSecret = []byte("your_secret_key") // Replace with your own secret key

// Custom context key type to avoid collisions
type contextKey string

const usernameKey contextKey = "username"

// Middleware to protect routes
func AuthMiddleware(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		tokenStr := r.Header.Get("Authorization")
		if tokenStr == "" {
			http.Error(w, "Authorization header missing", http.StatusUnauthorized)
			return
		}

		token, err := jwt.Parse(tokenStr, func(token *jwt.Token) (interface{}, error) {
			if _, ok := token.Method.(*jwt.SigningMethodHMAC); !ok {
				return nil, jwt.NewValidationError("invalid signing method", jwt.ValidationErrorUnverifiable)
			}
			return jwtSecret, nil
		})

		if err != nil || !token.Valid {
			http.Error(w, "Invalid token", http.StatusUnauthorized)
			return
		}

		claims, ok := token.Claims.(jwt.MapClaims)
		if !ok || !token.Valid {
			http.Error(w, "Invalid token claims", http.StatusUnauthorized)
			return
		}

		// Create a new context with the username value
		ctx := context.WithValue(r.Context(), usernameKey, claims["username"])
		
		// Pass the new context to the next handler
		r = r.WithContext(ctx)
		next.ServeHTTP(w, r)
	})
}
