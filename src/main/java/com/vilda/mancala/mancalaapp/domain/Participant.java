package com.vilda.mancala.mancalaapp.domain;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "participant")
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Participant {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid2")
    @Column(name = "id", nullable = false, length = 36)
    private String id;

/*    @EqualsAndHashCode.Include
    @Column(name = "external_id", nullable = false)
    private Integer externalId;*/

    @EqualsAndHashCode.Include
    @Column(name = "last_stone_in_big_pit", nullable = false)
    private Integer lastStoneInBigPit;

    @ManyToMany(cascade = {
            CascadeType.PERSIST,
            CascadeType.MERGE
    })
    @JoinTable(name = "mancala_game_participant",
            joinColumns = @JoinColumn(name = "game_id"),
            inverseJoinColumns = @JoinColumn(name = "participant_id")
    )
    private List<MancalaGame> mancalaGameList = new ArrayList<>();

/*    @OneToMany(mappedBy = "gamePlayer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MancalaGame> gameList = new ArrayList<>();*/

    @OneToMany(mappedBy = "pitPlayer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Pit> playerPitList = new ArrayList<>();

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="player_id", nullable=false)
    private Player player;
}
