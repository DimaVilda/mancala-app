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

/*    @Column(name = "mancala_game_id", nullable = false, length = 36)
    private String mancalaGameId;

    @Column(name = "player_account_id", nullable = false, length = 36)
    private String playerAccountId;*/

    @ManyToOne()
    @JoinColumn(name = "mancala_game_id")
    private MancalaGame mancalaGame;

    @ManyToOne()
    @JoinColumn(name = "player_account_id")
    private PlayerAccount playerAccount;

    @OneToMany(mappedBy = "participant", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Move> moveList = new ArrayList<>();

    @OneToMany(mappedBy = "participant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Pit> playerPitList = new ArrayList<>();

    @EqualsAndHashCode.Include
    @Column(name = "player_number", nullable = false)
    private Integer playerNumber;
}
