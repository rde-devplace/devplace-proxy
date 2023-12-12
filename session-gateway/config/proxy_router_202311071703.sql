INSERT INTO mydevdb.proxy_router (tokenRelay,headerName,headerValue,host,method,path,pathPattern,pathReplacement,uri,userName) VALUES
	 (1,NULL,NULL,NULL,'GET,POST,PUT,DELETE,PATCH,OPTIONS','/api/ideconfig/ide','/(?<segment>.*)','/${segment}','http://ide-operator-service:8080','init'),
	 (1,NULL,NULL,NULL,'GET,POST,PUT,DELETE,PATCH,OPTIONS','/api/ideconfig/ide4','/(?<segment>.*)','/${segment}','http://ide-operator-service:8080','init'),
	 (1,NULL,NULL,NULL,'GET,POST,PUT,DELETE,PATCH,OPTIONS','/console/**','/(?<segment>.*)','/${segment}','http://frontend-ide-service:8080','init'),
	 (1,NULL,NULL,NULL,'GET,POST,PUT,DELETE,PATCH,OPTIONS','/himang10/cli/**','/(?<segment>.*)','/${segment}','http://himang10-vscode-server-service:3000','himang10'),
	 (1,NULL,NULL,NULL,'GET,POST,PUT,DELETE,PATCH,OPTIONS','/himang10/frontend/console/**','/(?<segment>.*)','/${segment}','http://himang10-vscode-server-service:9090','himang10'),
	 (1,NULL,NULL,NULL,'GET,POST,PUT,DELETE,PATCH,OPTIONS','/himang10/proxy/8080/**','/himang10/proxy/8080/(?<segment>.*)','/${segment}','http://himang10-vscode-server-service:8080','himang10'),
	 (1,NULL,NULL,NULL,'GET,POST,PUT,DELETE,PATCH,OPTIONS','/himang10/proxy/9090/**','/himang10/proxy/9090/(?<segment>.*)','/${segment}','http://himang10-vscode-server-service:9090','himang10'),
	 (1,NULL,NULL,NULL,'GET,POST,PUT,DELETE,PATCH,OPTIONS','/himang10/vscode/**','/himang10/(?<segment>.*)','/${segment}','http://himang10-vscode-server-service:8443','himang10'),
	 (1,NULL,NULL,NULL,'GET,POST,PUT,DELETE,PATCH,OPTIONS','/test/my/**','/test/my/(?<segment>.*)','/${segment}','http://todo-server-service:8080','himang10');
