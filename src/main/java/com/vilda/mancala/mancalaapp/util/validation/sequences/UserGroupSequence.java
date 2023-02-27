package com.vilda.mancala.mancalaapp.util.validation.sequences;

import com.vilda.mancala.mancalaapp.util.validation.groups.NameGroup;

import javax.validation.GroupSequence;

/**
 * To specify order for groups validation
 */
@GroupSequence({
        NameGroup.class // TODO can be upgraded by adding some new groups, i.e. MailGroup to check mail, PasswordGroup to check password strictness
})
public interface UserGroupSequence {
}
