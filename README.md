```text
[Internet / Browser]
                                  |
                    HTTPS (Port 443, TLS via Let's Encrypt)
                                  |
                           +---------------+
                           |    NGINX      |
                           | (Reverse Proxy|
                           |   + Static    |
                           |     Hosting)  |
                           +---------------+
                              |         |
                              |         |
  Serviert Angular Files ---> |         | ---> Leitet REST-API-Calls weiter
                              |         |
                        /app/* (static)  /api/* (proxy_pass)
                              |         |
                              v         v
                    +------------------------+
                    |     WildFly Server     |
                    |  (REST API Backend)    |
                    |  - JAX-RS Endpoints    |
                    |  - JPA / JDBC          |
                    +------------------------+
                                  |
                         JDBC (Port 1521)
                                  |
                    +------------------------+
                    |     Oracle XE DB       |
                    | (Docker-Container)     |
                    +------------------------+
