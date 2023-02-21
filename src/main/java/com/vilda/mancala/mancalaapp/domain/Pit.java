package com.vilda.mancala.mancalaapp.domain;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Entity
@Table(name = "pit")
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Pit {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid2")
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    @Min(1)
    @Max(14)
    @EqualsAndHashCode.Include
    @Column(name = "pit_index", nullable = false)
    private Integer pitIndex;

    @ManyToOne
    @JoinColumn(name = "pit_id", nullable = false)
    private TableCurrentState tableCurrentState;

    @EqualsAndHashCode.Include
    @Column(name = "is_big_pit", nullable = false)
    private Integer isBigPit;

    @ManyToOne
    @JoinColumn(name = "participant_id")
    private Participant participant;

/*    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "game_id", nullable = false)
    private MancalaGame mancalaGame;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "player_id", nullable = false)
    private Player pitPlayer;*/
}
