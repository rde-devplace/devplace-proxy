
CREATE TABLE IF NOT EXISTS security_context (
    sessionId VARCHAR(255) NOT NULL,
    authentication TEXT,
    PRIMARY KEY (sessionId)
    );
-- mydevdb.proxy_router_1 definition

CREATE TABLE `proxy_router` (
                                  `tokenRelay` bit(1) NOT NULL DEFAULT b'0',
                                  `headerName` varchar(255) DEFAULT NULL,
                                  `headerValue` varchar(255) DEFAULT NULL,
                                  `host` varchar(255) DEFAULT NULL,
                                  `method` varchar(255) DEFAULT NULL,
                                  `path` varchar(255) NOT NULL,
                                  `pathPattern` varchar(255) DEFAULT NULL,
                                  `pathReplacement` varchar(255) DEFAULT NULL,
                                  `portNumber` varchar(255) NOT NULL,
                                  `svcFullName` varchar(255) NOT NULL,
                                  `uri` varchar(255) DEFAULT NULL,
                                  `userName` varchar(255) DEFAULT NULL,
                                  PRIMARY KEY (`path`,`portNumber`,`svcFullName`),
                                  KEY `idx_username` (`userName`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
