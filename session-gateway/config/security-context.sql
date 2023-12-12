CREATE TABLE `security_context` (
  `sessionId` VARCHAR(255) NOT NULL,
  `authentication` TEXT NOT NULL,
  PRIMARY KEY (`sessionId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

