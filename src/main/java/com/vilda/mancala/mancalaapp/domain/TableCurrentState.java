package com.vilda.mancala.mancalaapp.domain;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
@Table(name = "table_current_state")
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TableCurrentState {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid2")
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    @ManyToOne
    @JoinColumn(name = "mancala_game_id")
    private MancalaGame mancalaGame;

    @ManyToOne
    @JoinColumn(name = "pit_id", nullable = false)
    private Pit pit;

    @EqualsAndHashCode.Include
    @Column(name = "stones_count_in_pit", nullable = false)
    private Integer stonesCountInPit;

}
