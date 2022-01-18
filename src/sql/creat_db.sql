CREATE TABLE `user`(
    ID int AUTO_INCREMENT PRIMARY KEY,
    `username` varchar(255) UNIQUE,
    `password` varchar(255),
    nickname varchar(255),
    avatar varchar(255),
    numberOfGame int DEFAULT 0,
    numberOfWin int DEFAULT 0,
    numberOfDraw int DEFAULT 0,
    IsOnline int DEFAULT 0,
    IsPlaying int DEFAULT 0
);
CREATE TABLE friend(
    ID_User1 int NOT NULL,
    ID_User2 int NOT NULL,
    FOREIGN KEY (ID_User1) REFERENCES `user`(ID),
    FOREIGN KEY (ID_User2) REFERENCES `user`(ID),
    CONSTRAINT PK_friend PRIMARY KEY (ID_User1,ID_User2)
);
CREATE TABLE BANNED_USER(
    ID_User int PRIMARY KEY NOT NULL,
    FOREIGN KEY (ID_User) REFERENCES `user`(ID)
);