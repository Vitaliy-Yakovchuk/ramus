package com.ramussoft.common.journal.command;

import java.io.IOException;

import com.ramussoft.common.IEngine;
import com.ramussoft.common.journal.BinaryDataInput;
import com.ramussoft.common.journal.BinaryDataOutput;
import com.ramussoft.common.journal.JournaledEngine;

/**
 * This class exists only to restore branch, not for undo
 */

public class NewBranchCommand extends Command {

    /**
     *
     */
    private static final long serialVersionUID = -939957378469909715L;
    private long parentBranchId;
    private long branchId;
    private String reason;
    private int type;
    private String module;

    public NewBranchCommand(JournaledEngine engine) {
        super(engine);
    }

    public NewBranchCommand(JournaledEngine engine, long parentBranchId,
                            long branchId, String reason, int type, String module) {
        super(engine);
        this.parentBranchId = parentBranchId;
        this.branchId = branchId;
        this.reason = reason;
        this.type = type;
        this.module = module;
    }

    @Override
    public void writeBody(BinaryDataOutput output) throws IOException {
        output.writeLong(parentBranchId);
        output.writeLong(branchId);
        output.writeString(reason);
        output.writeInt(type);
        output.writeString(module);
    }

    @Override
    public void readBody(BinaryDataInput input) throws IOException {
        parentBranchId = input.readLong();
        branchId = input.readLong();
        reason = input.readString();
        type = input.readInt();
        module = input.readString();
    }

    @Override
    public void redo(IEngine engine) {
        engine.createBranch(parentBranchId, branchId, reason, type, module);
    }

    @Override
    public void undo(IEngine engine) {
        throw new RuntimeException("Undo not supported for new branch command");
    }

}
