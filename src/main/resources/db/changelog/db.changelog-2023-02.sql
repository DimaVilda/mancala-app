DROP TABLE IF EXISTS mancala_game;
DROP TABLE IF EXISTS player_account;
DROP TABLE IF EXISTS participant;
DROP TABLE IF EXISTS pit;
DROP TABLE IF EXISTS move;
DROP TABLE IF EXISTS table_current_state;

CREATE TABLE player_account
(
    id          VARCHAR(36) NOT NULL,
    player_name VARCHAR(36) NOT NULL,

    CONSTRAINT pk_player_account PRIMARY KEY (id)
);

CREATE TABLE mancala_game
(
    id                       VARCHAR(36) NOT NULL,
    game_status              VARCHAR(36) NOT NULL,
    last_participant_id_move VARCHAR(36) DEFAULT 0,
    second_turn              TINYINT    DEFAULT 0,

    CONSTRAINT pk_mancala_game PRIMARY KEY (id)
);

CREATE TABLE participant
(
    id                VARCHAR(36) NOT NULL,
    mancala_game_id   VARCHAR(36) NOT NULL,
    player_account_id VARCHAR(36) NOT NULL,
    player_number     TINYINT    NOT NULL,

    CONSTRAINT pk_participant PRIMARY KEY (id),
    CONSTRAINT fk_participant_mancala_game FOREIGN KEY (mancala_game_id) REFERENCES mancala_game (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT fk_participant_player_account FOREIGN KEY (player_account_id) REFERENCES player_account (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

-- info table about pits
CREATE TABLE pit
(
    id             VARCHAR(36) NOT NULL,
    participant_id VARCHAR(36) NOT NULL, --owner_part_id
    is_big_pit     TINYINT     NOT NULL,
    pit_index      TINYINT     NOT NULL,

    CONSTRAINT pk_pit PRIMARY KEY (id),
    CONSTRAINT fk_pit_participant FOREIGN KEY (participant_id) REFERENCES participant (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE TABLE move
(
    id                   VARCHAR(36) NOT NULL,
    move_number          INTEGER     NOT NULL,
    participant_id       VARCHAR(36) NOT NULL,
    from_pit_id          VARCHAR(36) DEFAULT 0,
    to_pit_id            VARCHAR(36) DEFAULT 0,
    is_fixed              TINYINT     DEFAULT 0,
    stones_count_in_hand TINYINT     NOT NULL,

    CONSTRAINT pk_move PRIMARY KEY (id),
    CONSTRAINT fk_move_participant FOREIGN KEY (participant_id) REFERENCES participant (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE TABLE table_current_state
(
    id                  VARCHAR(36) NOT NULL,
    mancala_game_id     VARCHAR(36) NOT NULL,
    pit_id              VARCHAR(36) NOT NULL,
    stones_count_in_pit TINYINT     NOT NULL,

    CONSTRAINT pk_table_current_state PRIMARY KEY (id),
    CONSTRAINT fk_table_current_state_mancala_game FOREIGN KEY (mancala_game_id) REFERENCES mancala_game (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT fk_table_current_state_pit FOREIGN KEY (pit_id) REFERENCES pit (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);