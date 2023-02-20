package com.vilda.mancala.mancalaapp.domain;

import com.vilda.mancala.mancalaapp.domain.enums.GameStatesEnum;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "mancala_game")
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class MancalaGame {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid2")
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    @Enumerated(EnumType.STRING)
    @EqualsAndHashCode.Include
    @Column(name = "game_status", nullable = false, length = 16)
    private GameStatesEnum gameStatus;

    @OneToMany(mappedBy = "mancalaGame", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Pit> pitList = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @MapsId
    @JoinColumn(name = "player_id_move")
    private Player gamePlayer;

    @ManyToMany(mappedBy = "mancalaGameList")
    private List<Participant> participantList = new ArrayList<>();
}
