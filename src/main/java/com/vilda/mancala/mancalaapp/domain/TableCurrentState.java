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
public class TableCurrentState {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid2")
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    @ManyToOne()
    @JoinColumn(name = "mancala_game_id")
    private MancalaGame mancalaGame;

    @OneToMany(mappedBy = "tableCurrentState", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Pit> pitList = new ArrayList<>();

    @EqualsAndHashCode.Include
    @Column(name = "stones_count_in_pit", nullable = false)
    private Integer stonesCountInPit;

}
