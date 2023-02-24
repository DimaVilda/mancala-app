package com.vilda.mancala.mancalaapp.domain;

import com.vilda.mancala.mancalaapp.domain.enums.GameStatesEnum;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    @EqualsAndHashCode.Include
    @Column(name = "last_participant_id_move", length = 36)
    private String lastParticipantIdMove;

    @EqualsAndHashCode.Include
    @Column(name = "second_turn", nullable = false)
    private Integer secondTurn;

    @OneToMany(mappedBy = "mancalaGame", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Participant> participantSet = new HashSet<>();

    @OneToMany(mappedBy = "mancalaGame", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TableCurrentState> tableCurrentStatesList = new ArrayList<>();

}
