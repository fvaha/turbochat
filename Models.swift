import Foundation

// 1. UserLogin
struct UserLogin: Codable {
    let username: String
    let password: String
}

// 2. LoginRequest
struct LoginRequest: Codable {
    let username: String
    let password: String
}

// 3. LoginResponse
struct LoginResponse: Codable {
    let success: Bool
    let message: String
    let data: UserData?
}

// 4. UserData
struct UserData: Codable {
    let id: Int64
    let username: String
    let publicKey: String
}

// 5. UserRegistration
struct UserRegistration: Codable {
    let username: String
    let password: String
    let publickey1: String
    let publickey2: String
    let publickey3: String
    let publickey4: String
}

// 6. User
struct User: Codable {
    let id: String
    let username: String
    let publicKey: String?
    let friends: [User]?
}

// 7. ApiUser
struct ApiUser: Codable {
    let id: String
    let username: String
    let publicKey: String
    let friends: [ApiUser]?

    enum CodingKeys: String, CodingKey {
        case id
        case username
        case publicKey = "publickey" // Maps to JSON key 'publickey'
        case friends
    }
}

// Extension function to convert ApiUser to User
extension ApiUser {
    func toUser() -> User {
        return User(
            id: self.id,
            username: self.username,
            publicKey: self.publicKey,
            friends: nil
        )
    }
}

// 8. Message
struct Message: Codable {
    let sender: String
    let recipient: String
    let content: String
}

// 9. PublicKeyResponse
struct PublicKeyResponse: Codable {
    let publicKey: String
}

// 10. AddFriendRequest
struct AddFriendRequest: Codable {
    let senderUsername: String
    let receiverUsername: String

    enum CodingKeys: String, CodingKey {
        case senderUsername = "sender_username"
        case receiverUsername = "receiver_username"
    }
}

// 11. FriendRequest
struct FriendRequest: Codable {
    let id: Int
    let senderUsername: String
    let receiverUsername: String

    enum CodingKeys: String, CodingKey {
        case id
        case senderUsername = "sender_username"
        case receiverUsername = "receiver_username"
    }
}

// 12. PendingRequest
struct PendingRequest: Codable {
    let id: Int
    let senderUsername: String
    let receiverUsername: String
}

// 13. DeclineFriendRequestResponse
struct DeclineFriendRequestResponse: Codable {
    let status: String
}

// 14. RemoveFriendRequest
struct RemoveFriendRequest: Codable {
    let username: String
    let friendUsername: String
}

// 15. Friend
struct Friend: Codable {
    let username: String
}

// 16. FriendListResponse
struct FriendListResponse: Codable {
    let friends: [Friend]
}
