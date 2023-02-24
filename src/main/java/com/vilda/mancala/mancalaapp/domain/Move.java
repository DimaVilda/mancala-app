package com.vilda.mancala.mancalaapp.domain;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
@Table(name = "move")
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Move {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid2")
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    //@Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "move_number", updatable = false, nullable = false)
    private Integer moveNumber;

/*    @ManyToOne
    @JoinColumn(name = "game_table_id", nullable = false)
    private GameTable gameTable;*/

    @ManyToOne
    @JoinColumn(name = "participant_id", nullable = false)
    private Participant participant;

    @EqualsAndHashCode.Include
    @Column(name = "from_pit_id", length = 36)
    private String fromPitId;

    @EqualsAndHashCode.Include
    @Column(name = "to_pit_id", length = 36)
    private String toPitId;

    @EqualsAndHashCode.Include
    @Column(name = "isFixed", nullable = false, length = 36)
    private Integer isFixed;

    @EqualsAndHashCode.Include
    @Column(name = "stones_count_in_hand", nullable = false, length = 36)
    private Integer stonesCountInHand;
}
