--<ScriptOptions statementTerminator=";"/>

DROP INDEX didlobject_pkey;

DROP INDEX musictrack_pkey;

DROP INDEX container_didlobject_children_id_key;

DROP INDEX fileres_pkey;

DROP INDEX musicalbum_pkey;

DROP INDEX res_pkey;

DROP INDEX musicgenre_pkey;

DROP INDEX crawler_pkey;

DROP INDEX blobres_pkey;

DROP INDEX externalres_pkey;

DROP INDEX audioitem_pkey;

DROP INDEX item_pkey;

DROP INDEX container_pkey;

DROP INDEX audiobroadcast_pkey;

DROP INDEX artistwithrole_pkey;

DROP TABLE didlobject_res;

DROP TABLE didlobject;

DROP TABLE container_didlobject;

DROP TABLE container;

DROP TABLE res;

DROP TABLE audioitem_publisher;

DROP TABLE fileres;

DROP TABLE audioitem;

DROP TABLE externalres;

DROP TABLE musictrack;

DROP TABLE album;

DROP TABLE item;

DROP TABLE didlobject_artist;

DROP TABLE artistwithrole;

DROP TABLE didlobject_genre;

DROP TABLE blobres;

DROP TABLE musicalbum;

DROP TABLE crawler;

DROP TABLE musicgenre;

DROP TABLE didlobject_resources;

DROP TABLE audiobroadcast;

CREATE TABLE didlobject_res (
		didlobject_id VARCHAR(255) NOT NULL,
		resources_id VARCHAR(255) NOT NULL
	);

CREATE TABLE didlobject (
		id VARCHAR(255) NOT NULL,
		albumarturi VARCHAR(255),
		class VARCHAR(255),
		creator VARCHAR(255),
		index INT4 NOT NULL,
		restricted BOOL NOT NULL,
		title VARCHAR(255),
		parentid VARCHAR(255)
	);

CREATE TABLE container_didlobject (
		container_id VARCHAR(255) NOT NULL,
		children_id VARCHAR(255) NOT NULL
	);

CREATE TABLE container (
		searchable BOOL NOT NULL,
		id VARCHAR(255) NOT NULL
	);

CREATE TABLE res (
		id VARCHAR(255) NOT NULL,
		duration VARCHAR(255),
		protocolinfo VARCHAR(255),
		size INT8 NOT NULL,
		type VARCHAR(255)
	);

CREATE TABLE audioitem_publisher (
		audioitem_id VARCHAR(255) NOT NULL,
		publisher VARCHAR(255)
	);

CREATE TABLE fileres (
		internaluri VARCHAR(512),
		id VARCHAR(255) NOT NULL
	);

CREATE TABLE audioitem (
		description VARCHAR(255),
		language VARCHAR(255),
		longdescription VARCHAR(255),
		relation VARCHAR(255),
		rights VARCHAR(255),
		id VARCHAR(255) NOT NULL
	);

CREATE TABLE externalres (
		uri VARCHAR(512),
		id VARCHAR(255) NOT NULL
	);

CREATE TABLE musictrack (
		album VARCHAR(255),
		originaldiscnumber INT4 NOT NULL,
		originaltracknumber INT4 NOT NULL,
		id VARCHAR(255) NOT NULL
	);

CREATE TABLE album (
		contributor VARCHAR(255),
		date DATE,
		description VARCHAR(255),
		longdescription VARCHAR(255),
		publisher VARCHAR(255),
		relation VARCHAR(255),
		rights VARCHAR(255),
		id VARCHAR(255) NOT NULL
	);

CREATE TABLE item (
		refid VARCHAR(255),
		id VARCHAR(255) NOT NULL
	);


CREATE TABLE didlobject_artist (
		object_id VARCHAR(255) NOT NULL,
		artists_artist VARCHAR(255) NOT NULL,
		artists_role INT4 NOT NULL
	);

CREATE TABLE artistwithrole (
		artist VARCHAR(255) NOT NULL,
		role INT4 NOT NULL
	);

CREATE TABLE didlobject_genre (
		didlobject_id VARCHAR(255) NOT NULL,
		genre VARCHAR(255)
	);

CREATE TABLE blobres (
		blob OID,
		id VARCHAR(255) NOT NULL
	);

CREATE TABLE musicalbum (
		id VARCHAR(255) NOT NULL
	);

CREATE TABLE crawler (
		path VARCHAR(512) NOT NULL,
		modified TIMESTAMP NOT NULL,
		state INT4 NOT NULL
	);

CREATE TABLE musicgenre (
		id VARCHAR(255) NOT NULL
	);

CREATE TABLE didlobject_resources (
		didlobject_id VARCHAR(255) NOT NULL,
		protocolinfo VARCHAR(255),
		uri VARCHAR(512)
	);

CREATE TABLE audiobroadcast (
		channelnr INT4 NOT NULL,
		radioband VARCHAR(255),
		radiocallsign VARCHAR(255),
		radiostationid VARCHAR(255),
		region VARCHAR(255),
		id VARCHAR(255) NOT NULL
	);

ALTER TABLE audioitem ADD CONSTRAINT audioitem_pkey PRIMARY KEY (id);

ALTER TABLE musicalbum ADD CONSTRAINT musicalbum_pkey PRIMARY KEY (id);

ALTER TABLE crawler ADD CONSTRAINT crawler_pkey PRIMARY KEY (path);

ALTER TABLE musicgenre ADD CONSTRAINT musicgenre_pkey PRIMARY KEY (id);

ALTER TABLE audiobroadcast ADD CONSTRAINT audiobroadcast_pkey PRIMARY KEY (id);

ALTER TABLE item ADD CONSTRAINT item_pkey PRIMARY KEY (id);

ALTER TABLE container ADD CONSTRAINT container_pkey PRIMARY KEY (id);

ALTER TABLE musictrack ADD CONSTRAINT musictrack_pkey PRIMARY KEY (id);

ALTER TABLE album ADD CONSTRAINT album_pkey PRIMARY KEY (id);

ALTER TABLE didlobject ADD CONSTRAINT didlobject_pkey PRIMARY KEY (id);

ALTER TABLE res ADD CONSTRAINT res_pkey PRIMARY KEY (id);

ALTER TABLE blobres ADD CONSTRAINT blobres_pkey PRIMARY KEY (id);

ALTER TABLE artistwithrole ADD CONSTRAINT artistwithrole_pkey PRIMARY KEY (artist, role);

ALTER TABLE externalres ADD CONSTRAINT externalres_pkey PRIMARY KEY (id);

ALTER TABLE fileres ADD CONSTRAINT fileres_pkey PRIMARY KEY (id);

ALTER TABLE didlobject ADD CONSTRAINT fk_didlobject_container FOREIGN KEY (parentid)
	REFERENCES container (id);

ALTER TABLE container ADD CONSTRAINT fk_container_didlobject FOREIGN KEY (id)
	REFERENCES didlobject (id);

ALTER TABLE fileres ADD CONSTRAINT fk_fileres FOREIGN KEY (id)
	REFERENCES res (id);

ALTER TABLE didlobject_artist ADD CONSTRAINT fk_artist_didlobject FOREIGN KEY (object_id)
	REFERENCES didlobject (id);

ALTER TABLE blobres ADD CONSTRAINT fk_blobres FOREIGN KEY (id)
	REFERENCES res (id);

ALTER TABLE audiobroadcast ADD CONSTRAINT fk_audiobroadcast FOREIGN KEY (id)
	REFERENCES audioitem (id);

ALTER TABLE item ADD CONSTRAINT fk_item FOREIGN KEY (id)
	REFERENCES didlobject (id);

ALTER TABLE didlobject_res ADD CONSTRAINT fk_didlobject_res FOREIGN KEY (didlobject_id)
	REFERENCES didlobject (id);

ALTER TABLE externalres ADD CONSTRAINT fk_externalres FOREIGN KEY (id)
	REFERENCES res (id);

ALTER TABLE audioitem ADD CONSTRAINT fk_audioitem FOREIGN KEY (id)
	REFERENCES item (id);

ALTER TABLE didlobject_res ADD CONSTRAINT fk_res FOREIGN KEY (resources_id)
	REFERENCES res (id);

ALTER TABLE musicgenre ADD CONSTRAINT fk_musicgenre FOREIGN KEY (id)
	REFERENCES container (id);

ALTER TABLE musicalbum ADD CONSTRAINT fk_musicalbum FOREIGN KEY (id)
	REFERENCES album (id);

ALTER TABLE audioitem_publisher ADD CONSTRAINT fk_audioitem_publisher FOREIGN KEY (audioitem_id)
	REFERENCES audioitem (id);

ALTER TABLE musictrack ADD CONSTRAINT fk_musictrack FOREIGN KEY (id)
	REFERENCES audioitem (id);

ALTER TABLE album ADD CONSTRAINT fk_album FOREIGN KEY (id)
	REFERENCES container (id);


