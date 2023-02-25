package com.vilda.mancala.mancalaapp.util.validation.sequences;

import com.vilda.mancala.mancalaapp.util.validation.groups.NameGroup;

import javax.validation.GroupSequence;

@GroupSequence({
        NameGroup.class
})
public interface UserGroupSequence {
}
