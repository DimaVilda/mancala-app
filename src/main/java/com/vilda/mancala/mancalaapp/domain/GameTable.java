/*
package com.vilda.mancala.mancalaapp.domain;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "game_table")
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class GameTable {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid2")
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    @OneToMany(mappedBy = "gameTable", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Participant> gameParticipant;

    @OneToMany(mappedBy = "gameTable", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Move> moveList = new ArrayList<>();

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="mancala_game_id", nullable=false)
    private MancalaGame mancalaGame;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="game_table_id", nullable=false)
    private TableCurrentState tableCurrentState;
}
*/
