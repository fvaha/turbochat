package main

import (
	"time"

	"github.com/golang-jwt/jwt/v4"
)

// Generate JWT
func generateJWT(username string) (string, error) {
	claims := &jwt.MapClaims{
		"username": username,
		"exp":      time.Now().Add(time.Hour * 24).Unix(), // Token expires in 24 hours
	}
	token := jwt.NewWithClaims(jwt.SigningMethodHS256, claims)
	return token.SignedString(jwtSecret)
}
