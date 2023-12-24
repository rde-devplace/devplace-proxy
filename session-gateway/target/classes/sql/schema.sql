CREATE TABLE IF NOT EXISTS proxy_router (
    path VARCHAR(255) NOT NULL,
    uri VARCHAR(255),
    method VARCHAR(255),
    host VARCHAR(255),
    headerName VARCHAR(255),
    headerValue VARCHAR(255),
    pathPattern VARCHAR(255),
    pathReplacement VARCHAR(255),
    tokenRelay BOOLEAN DEFAULT FALSE,
    userName VARCHAR(255),
    PRIMARY KEY (path),
    INDEX idx_username (userName)
    );

CREATE TABLE IF NOT EXISTS security_context (
    sessionId VARCHAR(255) NOT NULL,
    authentication TEXT,
    PRIMARY KEY (sessionId)
    );
