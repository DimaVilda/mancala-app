DROP TABLE IF EXISTS mancala_game;
DROP TABLE IF EXISTS player;
DROP TABLE IF EXISTS participant;
DROP TABLE IF EXISTS pit;

CREATE TABLE player
(
    id VARCHAR(36) NOT NULL,
    /*external_id TINYINT NOT NULL,*/
    player_name VARCHAR(36) NOT NULL,
    /*game_id VARCHAR(36) NOT NULL*/
    /*last_stone_in_big_pit TINYINT NOT NULL ,*/

    CONSTRAINT pk_player PRIMARY KEY (id)
/*    CONSTRAINT fk_player_mancala FOREIGN KEY (game_id) REFERENCES mancala_game (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION*/
);

CREATE TABLE mancala_game
(
    id           VARCHAR(36) NOT NULL,
    game_status  VARCHAR(16) NOT NULL,
    player_id_move  VARCHAR(36) NOT NULL ,

    CONSTRAINT pk_mancala_game PRIMARY KEY (id),
    CONSTRAINT fk_mancala_player FOREIGN KEY (player_id_move) REFERENCES player (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE TABLE participant
(
    id VARCHAR(36) NOT NULL,
    /*external_id TINYINT NOT NULL,*/
    game_id VARCHAR(36) NOT NULL,
    player_id VARCHAR(36) NOT NULL,
    last_stone_in_big_pit TINYINT NOT NULL ,

    CONSTRAINT pk_participant PRIMARY KEY (id),
    CONSTRAINT fk_participant_mancala FOREIGN KEY (game_id) REFERENCES mancala_game (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT fk_participant_player FOREIGN KEY (player_id) REFERENCES player (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);


CREATE TABLE pit
(
    id VARCHAR(36) NOT NULL,
    game_id      VARCHAR(36) NOT NULL,
    participant_id    VARCHAR(36) NOT NULL,
    pit_index    TINYINT     NOT NULL,
    stones_count TINYINT     NOT NULL,

    CONSTRAINT pk_pit PRIMARY KEY (id),
    CONSTRAINT fk_pit_mancala FOREIGN KEY (game_id) REFERENCES mancala_game (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT fk_pit_participant FOREIGN KEY (participant_id) REFERENCES participant (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
/*    CONSTRAINT fk_pit_player FOREIGN KEY (player_id) REFERENCES player (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION*/
);