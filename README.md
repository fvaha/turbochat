Here are all the curl commands based on your server's API endpoints:

1. Register a User
bash
Copy code
curl -X POST http://localhost:8080/register -H "Content-Type: application/json" -d '{"username":"testuser", "password":"testpassword"}'
2. Login a User
bash
Copy code
curl -X POST http://localhost:8080/login -H "Content-Type: application/json" -d '{"username":"testuser", "password":"testpassword"}'
3. Get a User's Public Key
bash
Copy code
curl -X GET "http://localhost:8080/keys?username=testuser"
4. Send a Message
bash
Copy code
curl -X POST http://localhost:8080/send_message -H "Content-Type: application/json" -d '{"sender":"testuser", "recipient":"anotheruser", "content":"Hello!"}'
5. Get Messages Between Two Users
bash
Copy code
curl -X GET "http://localhost:8080/messages?sender=testuser&recipient=anotheruser"
6. Send a Friend Request
bash
Copy code
curl -X POST http://localhost:8080/send_friend_request -H "Content-Type: application/json" -d '{"sender_username":"testuser", "receiver_username":"anotheruser"}'
7. Accept a Friend Request
bash
Copy code
curl -X POST http://localhost:8080/accept_friend_request -H "Content-Type: application/json" -d '{"sender_username":"testuser", "receiver_username":"anotheruser"}'
8. Decline a Friend Request
bash
Copy code
curl -X POST http://localhost:8080/decline_friend_request -H "Content-Type: application/json" -d '{"sender_username":"testuser", "receiver_username":"anotheruser"}'
9. Get Pending Friend Requests
bash
Copy code
curl -X GET "http://localhost:8080/pending_friend_requests?username=testuser"
10. Get Friends of a User
bash
Copy code
curl -X GET "http://localhost:8080/friends?username=testuser"
11. Add a Friend
bash
Copy code
curl -X POST http://localhost:8080/add_friend -H "Content-Type: application/json" -d '{"username":"testuser", "friend_username":"anotheruser"}'
12. Remove a Friend
bash
Copy code
curl -X POST http://localhost:8080/remove_friend -H "Content-Type: application/json" -d '{"username":"testuser", "friend_username":"anotheruser"}'
13. Get All Users
bash
Copy code
curl -X GET http://localhost:8080/users
Let me know if you need anything else!
