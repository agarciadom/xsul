CREATE TABLE cap_table (capid VARCHAR(20) NOT NULL PRIMARY KEY, ownerdn VARCHAR(40), handle VARCHAR(40) NOT NULL, notbefore VARCHAR(20) NOT NULL, notafter VARCHAR(20) NOT NULL, assertions LONGVARCHAR NOT NULL);
CREATE TABLE cap_user_table (capid VARCHAR(20) NOT NULL, groupname VARCHAR(40), userdn VARCHAR(40), FOREIGN KEY(capid) references cap_table(capid) ON DELETE CASCADE);
CREATE TABLE group_table (groupname VARCHAR(40) NOT NULL PRIMARY KEY, ownerdn VARCHAR(40), description VARCHAR(200) );
CREATE TABLE group_user_table (groupname VARCHAR(40) NOT NULL, userdn VARCHAR(40), FOREIGN KEY(groupname) REFERENCES group_table(groupname) ON DELETE CASCADE);
CREATE TABLE request_table (reqid VARCHAR(20) NOT NULL PRIMARY KEY, issuer VARCHAR(40) NOT NULL, resource VARCHAR(40) NOT NULL, actions VARCHAR(100) NOT NULL, status VARCHAR(10));
CREATE TABLE class_table (classid VARCHAR(20) NOT NULL, ownerdn VARCHAR(40), FOREIGN KEY(classid) REFERENCES group_table(groupname) ON DELETE CASCADE);
CREATE TABLE lesson_table (lessonid VARCHAR(20) NOT NULL PRIMARY KEY, subject VARCHAR(30) NOT NULL, instructor VARCHAR(40), link VARCHAR(60), acl VARCHAR(80), content LONGVARCHAR);
