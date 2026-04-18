Secure Web Server in Java

This project is a lightweight secure web server built entirely in Java using low level networking and file handling APIs. It was developed as part of a secure software assignment with the goal of understanding how web servers actually work under the hood, while applying strong security principles throughout the system.

Instead of relying on frameworks, everything in this project was implemented from scratch. This includes handling HTTP requests, serving files, processing form submissions, and enforcing security controls at multiple stages of the request lifecycle.

The result is a simple but well structured server that demonstrates both functionality and security awareness.

#Project Overview

The server listens for incoming client connections on a specified port and processes HTTP requests such as GET and POST. It is capable of serving static files, handling form submissions, and safely storing user input.

A key focus of the project is security. Multiple layers of protection have been built into the system to guard against common web vulnerabilities such as malicious input, directory traversal, and denial of service attempts.

The application is designed using a modular structure, separating responsibilities across different components such as request handling, logging, file management, and input sanitization.

#Features

Supports HTTP GET requests for serving static files

Supports HTTP POST requests for handling form submissions

Accepts user input via both GET query parameters and POST request bodies

Serves HTML files that render correctly in web browsers

Stores submitted form data securely on the server

Implements structured logging for monitoring and debugging

Handles multiple client requests using a thread pool

Returns proper HTTP status codes such as 200, 404, 400, 403, and 405

#Security Features

Input validation ensures that all incoming data follows expected formats and constraints

Input sanitization removes potentially harmful characters and patterns

Directory traversal protection prevents access to files outside the server root directory

Denial of service protection is implemented through request size limits and socket timeouts

Malformed requests are detected and safely rejected

Client IP addresses are logged to support auditing and traceability

POST request handling is isolated using a thread pool to reduce risk and improve stability

#Project Structure

The project is organised into multiple packages to improve readability and maintainability

server contains the main entry point and server setup

handlers manages request processing for GET and POST methods

utils provides helper classes for logging, file operations, and sanitization

http handles request and response formatting

config stores server configuration such as port and root directory

#How to Run

Compile the project

javac -d out src/server/.java src/handlers/.java src/utils/.java src/config/.java src/http/*.java

Run the server

java -cp out server.SimpleWebServer

Open your browser and go to

http://localhost:9090

Access Networking On a Virtual Machine:
http://10.0.2.15:9090

#Testing

The server can be tested using a browser for normal usage or tools such as Postman and netcat for more advanced testing.

Examples include sending GET requests to retrieve files, submitting forms using POST, and testing invalid or malicious inputs to observe how the server responds.

#Author

Micheal
