package main

import (
	"encoding/json"
	"errors" // For error handling
	"log"
	"net/http"

	"github.com/mattn/go-sqlite3"
	"golang.org/x/crypto/bcrypt" // Import bcrypt for password hashing
	"gorm.io/driver/sqlite"      // GORM's SQLite driver
	"gorm.io/gorm"               // GORM ORM for Go
)

// Define the User and Message models
type User struct {
	ID            uint   `json:"id" gorm:"primaryKey"`
	Username      string `json:"username" gorm:"unique"`
	Password      string `json:"password"` // Store hashed password
	PublicKey1    string `json:"publickey1"`
	PublicKey2    string `json:"publickey2"`
	PublicKey3    string `json:"publickey3"`
	PublicKey4    string `json:"publickey4"`
	Friends       []User `json:"friends" gorm:"many2many:user_friends;"`
}

type Message struct {
	ID        uint   `json:"id" gorm:"primaryKey"`
	Sender    string `json:"sender"`
	Recipient string `json:"recipient"`
	Content   string `json:"content"` // Encrypted message content
}

type FriendRequest struct {
	ID              uint      `json:"id" gorm:"primaryKey"`
	SenderUsername  string    `json:"sender_username"`
	ReceiverUsername string    `json:"receiver_username"`
}

var DB *gorm.DB

// Initialize the database (SQLite in this example)
func InitializeDatabase() {
	var err error
	DB, err = gorm.Open(sqlite.Open("test.db"), &gorm.Config{})
	if err != nil {
		log.Fatal("Failed to connect to database: ", err)
	}
	DB.AutoMigrate(&User{}, &Message{}, &FriendRequest{}) // Automatically create database schema
}

// Handler to register a new user with bcrypt password hashing
func registerUser(w http.ResponseWriter, r *http.Request) {
	var user User
	if err := json.NewDecoder(r.Body).Decode(&user); err != nil {
		log.Println("Error decoding user input:", err)
		w.Header().Set("Content-Type", "application/json")
		http.Error(w, `{"error": "Invalid input"}`, http.StatusBadRequest)
		return
	}

	// Hash the password before storing it
	hashedPassword, err := bcrypt.GenerateFromPassword([]byte(user.Password), bcrypt.DefaultCost)
	if err != nil {
		log.Println("Error hashing password:", err)
		w.Header().Set("Content-Type", "application/json")
		http.Error(w, `{"error": "Failed to hash password"}`, http.StatusInternalServerError)
		return
	}
	user.Password = string(hashedPassword)

	// Attempt to create the user
	err = DB.Create(&user).Error
	if err != nil {
		log.Println("Error creating user:", err) // Log the error
		var sqliteErr sqlite3.Error
		if errors.As(err, &sqliteErr) && sqliteErr.Code == sqlite3.ErrConstraint {
			w.Header().Set("Content-Type", "application/json")
			http.Error(w, `{"error": "User already exists"}`, http.StatusConflict)
			return
		}
		w.Header().Set("Content-Type", "application/json")
		http.Error(w, `{"error": "Internal server error"}`, http.StatusInternalServerError)
		return
	}

	// Send success response
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(http.StatusCreated)
	json.NewEncoder(w).Encode(user)
}

// Handler to login a user
func loginUser(w http.ResponseWriter, r *http.Request) {
	var user User
	if err := json.NewDecoder(r.Body).Decode(&user); err != nil {
		w.Header().Set("Content-Type", "application/json")
		http.Error(w, `{"success": false, "message": "Invalid input"}`, http.StatusBadRequest)
		return
	}

	var storedUser User
	if err := DB.Where("username = ?", user.Username).First(&storedUser).Error; err != nil {
		w.Header().Set("Content-Type", "application/json")
		http.Error(w, `{"success": false, "message": "Invalid username or password"}`, http.StatusUnauthorized)
		return
	}

	// Check if the provided password matches the stored password
	if err := bcrypt.CompareHashAndPassword([]byte(storedUser.Password), []byte(user.Password)); err != nil {
		w.Header().Set("Content-Type", "application/json")
		http.Error(w, `{"success": false, "message": "Invalid username or password"}`, http.StatusUnauthorized)
		return
	}

	// Send success response
	response := map[string]interface{}{
		"success": true,
		"message": "Login successful",
		"data": map[string]interface{}{
			"id":       storedUser.ID,
			"username": storedUser.Username,
			// Do not send public keys in response
		},
	}

	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(http.StatusOK) // User authenticated successfully
	json.NewEncoder(w).Encode(response)
}

// Handler to retrieve a user's public keys
func getPublicKeys(w http.ResponseWriter, r *http.Request) {
	username := r.URL.Query().Get("username")
	var user User
	if err := DB.Where("username = ?", username).First(&user).Error; err != nil {
		http.Error(w, `{"error": "User not found"}`, http.StatusNotFound)
		return
	}

	// Return all four public keys in the response
	json.NewEncoder(w).Encode(map[string]string{
		"publickey1": user.PublicKey1,
		"publickey2": user.PublicKey2,
		"publickey3": user.PublicKey3,
		"publickey4": user.PublicKey4,
	})
}

// Handler to send a friend request
func sendFriendRequest(w http.ResponseWriter, r *http.Request) {
	var request struct {
			SenderUsername   string `json:"sender_username"`
			ReceiverUsername string `json:"receiver_username"`
	}

	if err := json.NewDecoder(r.Body).Decode(&request); err != nil {
			http.Error(w, `{"error": "Invalid input"}`, http.StatusBadRequest)
			return
	}

	if request.SenderUsername == "" || request.ReceiverUsername == "" {
			http.Error(w, `{"error": "Sender and receiver usernames must not be empty"}`, http.StatusBadRequest)
			return
	}

	// Check if a friend request already exists
	var existingRequest FriendRequest
	if err := DB.Where("sender_username = ? AND receiver_username = ?", request.SenderUsername, request.ReceiverUsername).First(&existingRequest).Error; err == nil {
			http.Error(w, `{"error": "Friend request already exists"}`, http.StatusConflict)
			return
	}

	// Create the friend request with CustomTime
	friendRequest := FriendRequest{
			SenderUsername:   request.SenderUsername,
			ReceiverUsername: request.ReceiverUsername,
	}
	
	if err := DB.Create(&friendRequest).Error; err != nil {
			http.Error(w, `{"error": "Failed to send friend request"}`, http.StatusInternalServerError)
			log.Println("Error creating friend request:", err)
			return
	}

	w.WriteHeader(http.StatusOK)
	json.NewEncoder(w).Encode(map[string]string{"status": "Friend request sent"})
}


// Handler to accept a friend request
// Handler to accept a friend request
func acceptFriendRequest(w http.ResponseWriter, r *http.Request) {
	var request struct {
		SenderUsername   string `json:"sender_username"`
		ReceiverUsername string `json:"receiver_username"`
	}

	// Decode incoming JSON request
	if err := json.NewDecoder(r.Body).Decode(&request); err != nil {
		log.Printf("Error decoding request: %v", err)
		http.Error(w, `{"error": "Invalid input"}`, http.StatusBadRequest)
		return
	}

	// Check for empty fields in the request
	if request.SenderUsername == "" || request.ReceiverUsername == "" {
		log.Println("Sender or receiver username is empty")
		http.Error(w, `{"error": "Sender and receiver usernames must not be empty"}`, http.StatusBadRequest)
		return
	}

	// Log the sender and receiver usernames for debugging
	log.Printf("Accepting friend request from %s to %s", request.SenderUsername, request.ReceiverUsername)

	// Find the friend request in the database
	var friendRequest FriendRequest
	if err := DB.Where("sender_username = ? AND receiver_username = ?", request.SenderUsername, request.ReceiverUsername).First(&friendRequest).Error; err != nil {
		log.Printf("Friend request not found: %v", err)
		http.Error(w, `{"error": "Friend request not found"}`, http.StatusNotFound)
		return
	}

	// Find both users in the database
	var sender, receiver User
	if err := DB.Where("username = ?", request.SenderUsername).First(&sender).Error; err != nil {
		log.Printf("Sender not found: %v", err)
		http.Error(w, `{"error": "Sender not found"}`, http.StatusNotFound)
		return
	}
	if err := DB.Where("username = ?", request.ReceiverUsername).First(&receiver).Error; err != nil {
		log.Printf("Receiver not found: %v", err)
		http.Error(w, `{"error": "Receiver not found"}`, http.StatusNotFound)
		return
	}

	// Add both users as friends in the database
	if err := DB.Model(&sender).Association("Friends").Append(&receiver); err != nil {
		log.Printf("Failed to add friend for sender: %v", err)
		http.Error(w, `{"error": "Failed to add friend for sender"}`, http.StatusInternalServerError)
		return
	}
	if err := DB.Model(&receiver).Association("Friends").Append(&sender); err != nil {
		log.Printf("Failed to add friend for receiver: %v", err)
		http.Error(w, `{"error": "Failed to add friend for receiver"}`, http.StatusInternalServerError)
		return
	}

	// Remove the friend request from the database
	if err := DB.Delete(&friendRequest).Error; err != nil {
		log.Printf("Failed to delete friend request: %v", err)
		http.Error(w, `{"error": "Failed to delete friend request"}`, http.StatusInternalServerError)
		return
	}

	// Respond with success
	w.WriteHeader(http.StatusOK)
	json.NewEncoder(w).Encode(map[string]string{"status": "Friend request accepted"})
}




// Handler to decline a friend request
func declineFriendRequest(w http.ResponseWriter, r *http.Request) {
	var friendRequest FriendRequest

	// Decode the incoming JSON request into the FriendRequest struct
	if err := json.NewDecoder(r.Body).Decode(&friendRequest); err != nil {
		log.Printf("Error decoding request: %v", err)
		http.Error(w, `{"error": "Invalid input"}`, http.StatusBadRequest)
		return
	}

	// Check for empty fields
	if friendRequest.SenderUsername == "" || friendRequest.ReceiverUsername == "" {
		log.Println("Sender or receiver username is empty")
		http.Error(w, `{"error": "Sender and receiver usernames must not be empty"}`, http.StatusBadRequest)
		return
	}

	log.Printf("Declining friend request from %s to %s", friendRequest.SenderUsername, friendRequest.ReceiverUsername)

	// Attempt to find the friend request from the sender to receiver
	if err := DB.Where("sender_username = ? AND receiver_username = ?", friendRequest.SenderUsername, friendRequest.ReceiverUsername).First(&friendRequest).Error; err != nil {
		log.Printf("Friend request not found: %v", err)
		http.Error(w, `{"error": "Friend request not found"}`, http.StatusNotFound)
		return
	}

	// If found, delete the friend request
	if err := DB.Delete(&friendRequest).Error; err != nil {
		log.Printf("Error deleting friend request: %v", err)
		http.Error(w, `{"error": "Failed to decline friend request"}`, http.StatusInternalServerError)
		return
	}

	w.WriteHeader(http.StatusOK)
	json.NewEncoder(w).Encode(map[string]string{"status": "Friend request declined"})
}




// Handler to get pending friend requests
func getPendingFriendRequests(w http.ResponseWriter, r *http.Request) {
	username := r.URL.Query().Get("username")
	var requests []FriendRequest
	if err := DB.Where("receiver_username = ?", username).Find(&requests).Error; err != nil {
			http.Error(w, `{"error": "Failed to retrieve friend requests"}`, http.StatusInternalServerError)
			log.Printf("Error retrieving friend requests for %s: %v", username, err)
			return
	}

	// Log the requests being returned for debugging
	log.Printf("Pending friend requests for %s: %+v", username, requests)

	// Return the requests as JSON
	w.Header().Set("Content-Type", "application/json")
	if err := json.NewEncoder(w).Encode(requests); err != nil {
			http.Error(w, `{"error": "Failed to encode response"}`, http.StatusInternalServerError)
			log.Printf("Error encoding JSON response: %v", err)
			return
	}
}

// Handler to remove a friend
// Handler to remove a friend
func removeFriend(w http.ResponseWriter, r *http.Request) {
	var request struct {
			Username      string `json:"username"`
			FriendUsername string `json:"friend_username"`
	}
	if err := json.NewDecoder(r.Body).Decode(&request); err != nil {
			http.Error(w, `{"error": "Invalid input"}`, http.StatusBadRequest)
			return
	}

	var user User
	if err := DB.Where("username = ?", request.Username).First(&user).Error; err != nil {
			http.Error(w, `{"error": "User not found"}`, http.StatusNotFound)
			return
	}

	var friend User
	if err := DB.Where("username = ?", request.FriendUsername).First(&friend).Error; err != nil {
			http.Error(w, `{"error": "Friend not found"}`, http.StatusNotFound)
			return
	}

	// Remove both users from each other's friends list
	if err := DB.Model(&user).Association("Friends").Delete(&friend); err != nil {
			http.Error(w, `{"error": "Failed to remove friend"}`, http.StatusInternalServerError)
			return
	}
	if err := DB.Model(&friend).Association("Friends").Delete(&user); err != nil {
			http.Error(w, `{"error": "Failed to remove friend"}`, http.StatusInternalServerError)
			return
	}

	w.WriteHeader(http.StatusOK)
	json.NewEncoder(w).Encode(map[string]string{"status": "Friend removed"})
}



// Handler to send a message
func sendMessage(w http.ResponseWriter, r *http.Request) {
	var msg Message
	if err := json.NewDecoder(r.Body).Decode(&msg); err != nil {
		http.Error(w, `{"error": "Invalid input"}`, http.StatusBadRequest)
		return
	}
	if err := DB.Create(&msg).Error; err != nil {
		http.Error(w, `{"error": "Failed to send message"}`, http.StatusInternalServerError)
		return
	}
	w.WriteHeader(http.StatusOK)
}

// Handler to retrieve messages between two users
func getMessages(w http.ResponseWriter, r *http.Request) {
	sender := r.URL.Query().Get("sender")
	recipient := r.URL.Query().Get("recipient")

	var userMessages []Message
	if err := DB.Where("(sender = ? AND recipient = ?) OR (sender = ? AND recipient = ?)", sender, recipient, recipient, sender).Find(&userMessages).Error; err != nil {
		http.Error(w, `{"error": "Failed to retrieve messages"}`, http.StatusInternalServerError)
		return
	}
	json.NewEncoder(w).Encode(userMessages)
}
// Root handler
func rootHandler(w http.ResponseWriter, r *http.Request) {
	w.Write([]byte("Welcome to the chat server! Use /register, /login, etc."))
}

// Handler to get all usernames
// Handler to get all usernames
func getAllUsernames(w http.ResponseWriter, r *http.Request) {
	var users []User

	// Fetch all users from the database
	if err := DB.Find(&users).Error; err != nil {
			w.Header().Set("Content-Type", "application/json")
			http.Error(w, `{"error": "Failed to fetch users"}`, http.StatusInternalServerError)
			return
	}

	// Create a slice to hold only usernames
	usernames := make([]string, len(users))
	for i, user := range users {
			usernames[i] = user.Username // Populate the slice with usernames
	}

	// Send back the list of usernames as a JSON array
	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(usernames)
}


func getFriends(w http.ResponseWriter, r *http.Request) {
	username := r.URL.Query().Get("username")
	var user User
	if err := DB.Preload("Friends").Where("username = ?", username).First(&user).Error; err != nil {
			http.Error(w, `{"error": "User not found"}`, http.StatusNotFound)
			return
	}
	if len(user.Friends) == 0 {
			w.Header().Set("Content-Type", "application/json")
			json.NewEncoder(w).Encode([]User{}) // Send an empty list if no friends
	} else {
			json.NewEncoder(w).Encode(user.Friends)
	}
}


// Main function to start the server
// Main function to start the server
// Main function to start the server
func main() {
	log.Println("Initializing database...")
	InitializeDatabase() // Initialize the database

	log.Println("Setting up routes...")
	http.HandleFunc("/", rootHandler) // Add root handler
	http.HandleFunc("/register", registerUser)
	http.HandleFunc("/login", loginUser)
	http.HandleFunc("/keys", getPublicKeys)
	http.HandleFunc("/send_message", sendMessage)
	http.HandleFunc("/messages", getMessages)
	http.HandleFunc("/send_friend_request", sendFriendRequest)
	http.HandleFunc("/accept_friend_request", acceptFriendRequest)
	http.HandleFunc("/decline_friend_request", declineFriendRequest)
	http.HandleFunc("/pending_friend_requests", getPendingFriendRequests)
	http.HandleFunc("/remove_friend", removeFriend) // Ensure this is included
	http.HandleFunc("/friends", getFriends) // Ensure this is included
	http.HandleFunc("/users", getAllUsernames)

	log.Println("Server is running on port 8080")
	if err := http.ListenAndServe(":8080", nil); err != nil {
			log.Fatal(err)
	}
}

