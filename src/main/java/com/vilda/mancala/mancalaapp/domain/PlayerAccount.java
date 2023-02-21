package com.vilda.mancala.mancalaapp.domain;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "player_account")
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PlayerAccount {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid2")
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    @EqualsAndHashCode.Include
    @Column(name = "player_name", nullable = false, length = 36)
    private String playerName;

    @OneToMany(mappedBy = "playerAccount", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Participant> participantList = new ArrayList<>();
}
