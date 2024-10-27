import Foundation
import Alamofire

struct APIService {
    static let shared = APIService()
    private let baseURL = "http://localhost:8080"
    
    // Helper function for creating the URL
    private func makeURL(_ endpoint: String) -> String {
        return "\(baseURL)/\(endpoint)"
    }

    // MARK: - API Endpoints

    // 1. Register a User
    func registerUser(username: String, password: String, completion: @escaping (Result<Void, Error>) -> Void) {
        let parameters = ["username": username, "password": password]
        
        AF.request(makeURL("register"), method: .post, parameters: parameters, encoding: JSONEncoding.default)
            .validate()
            .response { response in
                switch response.result {
                case .success:
                    completion(.success(()))
                case .failure(let error):
                    completion(.failure(error))
                }
            }
    }

    // 2. Login a User
    func loginUser(username: String, password: String, completion: @escaping (Result<Void, Error>) -> Void) {
        let parameters = ["username": username, "password": password]
        
        AF.request(makeURL("login"), method: .post, parameters: parameters, encoding: JSONEncoding.default)
            .validate()
            .response { response in
                switch response.result {
                case .success:
                    completion(.success(()))
                case .failure(let error):
                    completion(.failure(error))
                }
            }
    }

    // 3. Get a User's Public Key
    func getUserPublicKey(username: String, completion: @escaping (Result<String, Error>) -> Void) {
        AF.request(makeURL("keys"), parameters: ["username": username])
            .validate()
            .responseDecodable(of: String.self) { response in
                completion(response.result)
            }
    }

    // 4. Send a Message
    func sendMessage(sender: String, recipient: String, content: String, completion: @escaping (Result<Void, Error>) -> Void) {
        let parameters = ["sender": sender, "recipient": recipient, "content": content]
        
        AF.request(makeURL("send_message"), method: .post, parameters: parameters, encoding: JSONEncoding.default)
            .validate()
            .response { response in
                completion(response.result.map { _ in () })
            }
    }

    // 5. Get Messages Between Two Users
    func getMessagesBetweenUsers(sender: String, recipient: String, completion: @escaping (Result<[String], Error>) -> Void) {
        AF.request(makeURL("messages"), parameters: ["sender": sender, "recipient": recipient])
            .validate()
            .responseDecodable(of: [String].self) { response in
                completion(response.result)
            }
    }

    // 6. Send a Friend Request
    func sendFriendRequest(senderUsername: String, receiverUsername: String, completion: @escaping (Result<Void, Error>) -> Void) {
        let parameters = ["sender_username": senderUsername, "receiver_username": receiverUsername]
        
        AF.request(makeURL("send_friend_request"), method: .post, parameters: parameters, encoding: JSONEncoding.default)
            .validate()
            .response { response in
                completion(response.result.map { _ in () })
            }
    }

    // 7. Accept a Friend Request
    func acceptFriendRequest(senderUsername: String, receiverUsername: String, completion: @escaping (Result<Void, Error>) -> Void) {
        let parameters = ["sender_username": senderUsername, "receiver_username": receiverUsername]
        
        AF.request(makeURL("accept_friend_request"), method: .post, parameters: parameters, encoding: JSONEncoding.default)
            .validate()
            .response { response in
                completion(response.result.map { _ in () })
            }
    }

    // 8. Decline a Friend Request
    func declineFriendRequest(senderUsername: String, receiverUsername: String, completion: @escaping (Result<Void, Error>) -> Void) {
        let parameters = ["sender_username": senderUsername, "receiver_username": receiverUsername]
        
        AF.request(makeURL("decline_friend_request"), method: .post, parameters: parameters, encoding: JSONEncoding.default)
            .validate()
            .response { response in
                completion(response.result.map { _ in () })
            }
    }

    // 9. Get Pending Friend Requests
    func getPendingFriendRequests(username: String, completion: @escaping (Result<[String], Error>) -> Void) {
        AF.request(makeURL("pending_friend_requests"), parameters: ["username": username])
            .validate()
            .responseDecodable(of: [String].self) { response in
                completion(response.result)
            }
    }

    // 10. Get Friends of a User
    func getFriends(username: String, completion: @escaping (Result<[String], Error>) -> Void) {
        AF.request(makeURL("friends"), parameters: ["username": username])
            .validate()
            .responseDecodable(of: [String].self) { response in
                completion(response.result)
            }
    }

    // 11. Add a Friend
    func addFriend(username: String, friendUsername: String, completion: @escaping (Result<Void, Error>) -> Void) {
        let parameters = ["username": username, "friend_username": friendUsername]
        
        AF.request(makeURL("add_friend"), method: .post, parameters: parameters, encoding: JSONEncoding.default)
            .validate()
            .response { response in
                completion(response.result.map { _ in () })
            }
    }

    // 12. Remove a Friend
    func removeFriend(username: String, friendUsername: String, completion: @escaping (Result<Void, Error>) -> Void) {
        let parameters = ["username": username, "friend_username": friendUsername]
        
        AF.request(makeURL("remove_friend"), method: .post, parameters: parameters, encoding: JSONEncoding.default)
            .validate()
            .response { response in
                completion(response.result.map { _ in () })
            }
    }

    // 13. Get All Users
    func getAllUsers(completion: @escaping (Result<[String], Error>) -> Void) {
        AF.request(makeURL("users"))
            .validate()
            .responseDecodable(of: [String].self) { response in
                completion(response.result)
            }
    }
}
