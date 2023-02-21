package com.vilda.mancala.mancalaapp.domain;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.ArrayList;
import java.util.List;

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

    @OneToMany(mappedBy = "pit", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TableCurrentState> tableCurrentStates = new ArrayList<>();

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
