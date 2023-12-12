INSERT INTO proxy_router (path, uri, method, host, headerName, headerValue, pathPattern, pathReplacement, tokenRelay, userName) VALUES
                                                                                                                                    ('/api/ideconfig/ide', 'http://ide-operator-service:8080', 'GET,POST,PUT,DELETE,PATCH,OPTIONS', NULL, NULL, NULL, '/(?<segment>.*)', '/${segment}', TRUE, 'init'),
                                                                                                                                    ('/api/ideconfig/ide4', 'http://ide-operator-service:8080', 'GET,POST,PUT,DELETE,PATCH,OPTIONS', NULL, NULL, NULL, '/(?<segment>.*)', '/${segment}', TRUE, 'init'),
                                                                                                                                    ('/console/**', 'http://frontend-ide-service:8080', 'GET,POST,PUT,DELETE,PATCH,OPTIONS', NULL, NULL, NULL, '/(?<segment>.*)', '/${segment}', TRUE, 'init'),
                                                                                                                                    ('/himang10/cli/**', 'http://himang10-vscode-server-service:3000', 'GET,POST,PUT,DELETE,PATCH,OPTIONS', NULL, NULL, NULL, '/(?<segment>.*)', '/${segment}', TRUE, 'himang10'),
                                                                                                                                    ('/himang10/frontend/console/**', 'http://himang10-vscode-server-service:9090', 'GET,POST,PUT,DELETE,PATCH,OPTIONS', NULL, NULL, NULL, '/(?<segment>.*)', '/${segment}', TRUE, 'himang10'),
                                                                                                                                    ('/himang10/proxy/8080/**', 'http://himang10-vscode-server-service:8080', 'GET,POST,PUT,DELETE,PATCH,OPTIONS', NULL, NULL, NULL, '/himang10/proxy/8080/(?<segment>.*)', '/${segment}', TRUE, 'himang10'),
                                                                                                                                    ('/himang10/proxy/9090/**', 'http://himang10-vscode-server-service:9090', 'GET,POST,PUT,DELETE,PATCH,OPTIONS', NULL, NULL, NULL, '/himang10/proxy/9090/(?<segment>.*)', '/${segment}', TRUE, 'himang10'),
                                                                                                                                    ('/himang10/vscode/**', 'http://himang10-vscode-server-service:8443', 'GET,POST,PUT,DELETE,PATCH,OPTIONS', NULL, NULL, NULL, '/himang10/(?<segment>.*)', '/${segment}', TRUE, 'himang10'),
                                                                                                                                    ('/test/my/**', 'http://todo-server-service:8080', 'GET,POST,PUT,DELETE,PATCH,OPTIONS', NULL, NULL, NULL, '/test/my/(?<segment>.*)', '/${segment}', TRUE, 'himang10')
ON DUPLICATE KEY UPDATE
                     uri = VALUES(uri), method = VALUES(method), host = VALUES(host), headerName = VALUES(headerName), headerValue = VALUES(headerValue), pathPattern = VALUES(pathPattern), pathReplacement = VALUES(pathReplacement), tokenRelay = VALUES(tokenRelay), userName = VALUES(userName);
