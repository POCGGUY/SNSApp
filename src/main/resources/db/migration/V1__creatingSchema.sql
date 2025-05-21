SET search_path TO SNS;

CREATE TABLE IF NOT EXISTS users(
	id SERIAL PRIMARY KEY,
	userName VARCHAR(20) NOT NULL UNIQUE CHECK (length(trim(userName)) >= 3),
	creationDate TIMESTAMPTZ NOT NULL,
	birthDate DATE NOT NULL,
	password VARCHAR(1000) NOT NULL,
	email VARCHAR(100) NOT NULL UNIQUE,
	firstName VARCHAR(50) NOT NULL CHECK (length(trim(firstName)) > 0),
	secondName VARCHAR(50) NOT NULL CHECK (length(trim(secondName)) > 0),
	thirdName VARCHAR(50) CHECK (length(trim(thirdName)) > 0),
	gender VARCHAR(100) NOT NULL,
	systemRole VARCHAR(100) NOT NULL,
	description VARCHAR(1000) CHECK (length(trim(description)) > 0),
	deleted BOOLEAN DEFAULT FALSE NOT NULL,
	acceptingPrivateMsgs BOOLEAN DEFAULT TRUE NOT NULL,
	postsPublic BOOLEAN DEFAULT TRUE NOT NULL,
	banned BOOLEAN DEFAULT FALSE NOT NULL,
	CONSTRAINT users_birthDate_constraint CHECK(
		EXTRACT(YEAR FROM AGE(CURRENT_DATE, birthDate)) BETWEEN 14 AND 150),
	CONSTRAINT gender_check CHECK(gender IN ('MALE', 'FEMALE')),
	CONSTRAINT system_role_check CHECK(systemRole IN ('USER', 'MODERATOR', 'ADMIN'))
);

CREATE TABLE IF NOT EXISTS communities(
	id SERIAL PRIMARY KEY,
	ownerId INT NOT NULL,
	communityName VARCHAR(100) NOT NULL CHECK (length(trim(communityName)) > 0),
	creationDate TIMESTAMPTZ NOT NULL,
	isPrivate BOOLEAN DEFAULT FALSE NOT NULL,
	description VARCHAR(1000) CHECK (length(trim(description)) > 0),
	deleted BOOLEAN DEFAULT FALSE NOT NULL,
	banned BOOLEAN DEFAULT FALSE NOT NULL,
	FOREIGN KEY (ownerId)
		REFERENCES users(id)
		ON DELETE CASCADE
		ON UPDATE CASCADE
);
CREATE INDEX owner_communities_idx ON communities(ownerId, id);

CREATE TABLE IF NOT EXISTS friendships(
	userId INT NOT NULL,
	friendId INT NOT NULL CHECK (userId <> friendId),
	creationDate TIMESTAMPTZ NOT NULL,
	PRIMARY KEY (userId, friendId),
	FOREIGN KEY (userId)
		REFERENCES users(id)
		ON DELETE CASCADE
		ON UPDATE CASCADE,
	FOREIGN KEY (friendId)
		REFERENCES users(id)
		ON DELETE CASCADE
		ON UPDATE CASCADE
);
CREATE INDEX user_friend_idx ON friendships(userId, friendId);
CREATE INDEX friend_user_idx ON friendships(friendId, userId);

CREATE TABLE IF NOT EXISTS friendship_requests(
	senderId INT NOT NULL,
	receiverId INT NOT NULL CHECK (senderId <> receiverId),
	creationDate TIMESTAMPTZ NOT NULL,
	PRIMARY KEY (senderId, receiverId),
	FOREIGN KEY (senderId)
		REFERENCES users(id)
		ON DELETE CASCADE
		ON UPDATE CASCADE,
	FOREIGN KEY (receiverId)
		REFERENCES users(id)
		ON DELETE CASCADE
		ON UPDATE CASCADE
);
CREATE INDEX sender_friendship_requests_idx ON friendship_requests(senderId, receiverId);
CREATE INDEX receiver_friendship_requests_idx ON friendship_requests(receiverId, senderId);

CREATE TABLE IF NOT EXISTS posts(
	id SERIAL PRIMARY KEY,
	ownerUserId INT CHECK((ownerUserId IS NULL) <> (ownerCommunityId IS NULL)),
	ownerCommunityId INT CHECK((ownerUserId IS NULL) <> (ownerCommunityId IS NULL)),
	creationDate TIMESTAMPTZ NOT NULL,
	authorId INT NOT NULL,
	updateDate TIMESTAMPTZ,
	deleted BOOLEAN DEFAULT FALSE NOT NULL,
	text VARCHAR(1000) NOT NULL CHECK (length(trim(text)) > 0),
	FOREIGN KEY (ownerUserId)
		REFERENCES users(id)
		ON DELETE CASCADE
		ON UPDATE CASCADE,
	FOREIGN KEY (ownerCommunityId)
		REFERENCES communities(id)
		ON DELETE CASCADE
		ON UPDATE CASCADE,
	FOREIGN KEY (authorId)
		REFERENCES users(id)
		ON DELETE CASCADE
		ON UPDATE CASCADE
);

CREATE INDEX owner_posts_idx ON posts(ownerUserId, id);
CREATE INDEX author_posts_idx ON posts(authorId, id);

CREATE TABLE IF NOT EXISTS posts_comments(
	id SERIAL PRIMARY KEY,
	postId INT NOT NULL,
	authorId INT NOT NULL,
	creationDate TIMESTAMPTZ,
	updateDate TIMESTAMPTZ,
	deleted BOOLEAN DEFAULT FALSE NOT NULL,
	text VARCHAR(1000) NOT NULL CHECK(length(trim(text)) > 0),
	FOREIGN KEY (postId)
		REFERENCES posts(id)
		ON DELETE CASCADE
		ON UPDATE CASCADE,
	FOREIGN KEY (authorId)
		REFERENCES users(id)
		ON DELETE CASCADE
		ON UPDATE CASCADE
);
CREATE INDEX post_comments_idx ON posts_comments(postId, id);
CREATE INDEX author_comments_idx ON posts_comments(authorId, id);

CREATE TABLE IF NOT EXISTS community_members(
	communityId INT NOT NULL,
	memberId INT NOT NULL,
	entryDate TIMESTAMPTZ NOT NULL,
	memberRole VARCHAR(100) NOT NULL,
	PRIMARY KEY (communityId, memberId),
	FOREIGN KEY (communityId)
		REFERENCES communities(id)
		ON DELETE CASCADE
		ON UPDATE CASCADE,
	FOREIGN KEY (memberId)
		REFERENCES users(id)
		ON DELETE CASCADE
		ON UPDATE CASCADE,
	CONSTRAINT unique_community_members UNIQUE(communityId, memberId),
	CONSTRAINT member_role_check CHECK(memberRole in ('MEMBER', 'MODERATOR', 'OWNER'))
);
CREATE INDEX community_members_idx ON community_members (communityId, memberId);
CREATE INDEX users_communities_idx ON community_members (memberId, communityId);

CREATE TABLE IF NOT EXISTS private_messages(
	id SERIAL PRIMARY KEY,
	senderId INT NOT NULL,
	receiverId INT NOT NULL,
	creationDate TIMESTAMPTZ NOT NULL,
	updateDate TIMESTAMPTZ,
	deleted BOOLEAN DEFAULT FALSE NOT NULL,
	text VARCHAR(1000) NOT NULL CHECK (length(trim(text)) > 0),
	FOREIGN KEY (senderId)
		REFERENCES users(id)
		ON DELETE CASCADE
		ON UPDATE CASCADE,
	FOREIGN KEY (receiverId)
		REFERENCES users(id)
		ON DELETE CASCADE
		ON UPDATE CASCADE
);
CREATE INDEX messages_sender_idx ON private_messages(senderId, receiverId);
CREATE INDEX messages_receiver_idx ON private_messages(receiverId, senderId);

CREATE TABLE IF NOT EXISTS community_invitations(
	senderId INT NOT NULL,
	receiverId INT NOT NULL CHECK (senderId <> receiverId),
	communityId INT NOT NULL,
	creationDate TIMESTAMPTZ NOT NULL,
	PRIMARY KEY (senderId, receiverId, communityId),
	description VARCHAR(1000),
	FOREIGN KEY (senderId)
		REFERENCES users(id)
		ON DELETE CASCADE
		ON UPDATE CASCADE,
	FOREIGN KEY (receiverId)
		REFERENCES users(id)
		ON DELETE CASCADE
		ON UPDATE CASCADE,
	FOREIGN KEY (communityId)
		REFERENCES communities(id)
		ON DELETE CASCADE
		ON UPDATE CASCADE
);
CREATE INDEX invitations_sender_idx ON community_invitations(senderId, receiverId);
CREATE INDEX invitations_receiver_idx ON community_invitations(receiverId, senderId);

CREATE TABLE IF NOT EXISTS chats(
	id SERIAL PRIMARY KEY,
	ownerId INT NOT NULL,
	name VARCHAR(100) NOT NULL,
	description VARCHAR(1000),
	creationDate TIMESTAMPTZ NOT NULL,
	deleted BOOLEAN DEFAULT FALSE NOT NULL,
	isPrivate BOOLEAN DEFAULT FALSE NOT NULL,
	FOREIGN KEY (ownerId)
		REFERENCES users(id)
		ON DELETE CASCADE
		ON UPDATE CASCADE
);
CREATE INDEX chats_owner_idx ON chats(ownerId, id);

CREATE TABLE IF NOT EXISTS chats_members(
	chatId INT NOT NULL,
	memberId INT NOT NULL,
	entryDate TIMESTAMPTZ NOT NULL,
	PRIMARY KEY (chatId, memberId),
	FOREIGN KEY (chatId)
		REFERENCES chats(id)
		ON DELETE CASCADE
		ON UPDATE CASCADE,
	FOREIGN KEY (memberId)
		REFERENCES users(id)
		ON DELETE CASCADE
		ON UPDATE CASCADE
);
CREATE INDEX chat_member_idx ON chats_members(chatId, memberId);
CREATE INDEX user_chat_idx ON chats_members(memberId, chatId);

CREATE TABLE IF NOT EXISTS chat_messages(
	id SERIAL PRIMARY KEY,
	chatId INT NOT NULL,
	senderId INT NOT NULL,
	updateDate TIMESTAMPTZ,
	deleted BOOLEAN DEFAULT FALSE NOT NULL,
	sendingDate TIMESTAMPTZ NOT NULL,
	text VARCHAR(1000) NOT NULL CHECK (length(trim(text)) > 0),
	FOREIGN KEY (chatId)
		REFERENCES chats(id)
		ON DELETE CASCADE
		ON UPDATE CASCADE,
	FOREIGN KEY (senderId)
		REFERENCES users(id)
		ON DELETE CASCADE
		ON UPDATE CASCADE
);
CREATE INDEX chat_messages_idx ON chat_messages(chatId, id);
CREATE INDEX sender_chat_messages_idx ON chat_messages(senderId, id);

CREATE TABLE IF NOT EXISTS chat_invitations(
	senderId INT NOT NULL,
	receiverId INT NOT NULL CHECK (senderId <> receiverId),
	creationDate TIMESTAMPTZ NOT NULL,
	chatId INT NOT NULL,
	PRIMARY KEY (chatId, senderId, receiverId),
	description VARCHAR(1000),
	FOREIGN KEY (senderId)
		REFERENCES users(id)
		ON DELETE CASCADE
		ON UPDATE CASCADE,
	FOREIGN KEY (receiverId)
		REFERENCES users(id)
		ON DELETE CASCADE
		ON UPDATE CASCADE,
	FOREIGN KEY (chatId)
		REFERENCES chats(id)
		ON DELETE CASCADE
		ON UPDATE CASCADE
);
CREATE INDEX sender_chat_invitations_idx ON chat_invitations(senderId, chatId);
CREATE INDEX receiver_chat_invitations_idx ON chat_invitations(receiverId, chatId);

CREATE TABLE IF NOT EXISTS notifications(
	id SERIAL PRIMARY KEY,
	receiverId INT NOT NULL,
	description VARCHAR(300) NOT NULL,
	creationDate TIMESTAMPTZ NOT NULL,
	read BOOLEAN DEFAULT FALSE NOT NULL,
	FOREIGN KEY (receiverId)
		REFERENCES users(id)
		ON DELETE CASCADE
		ON UPDATE CASCADE
);
CREATE INDEX receiver_notifications_idx ON notifications(receiverId, id);
