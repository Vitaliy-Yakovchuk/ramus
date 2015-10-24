package com.ramussoft.common.journal.command;

import java.io.IOException;

import com.ramussoft.common.CalculateInfo;
import com.ramussoft.common.IEngine;
import com.ramussoft.common.event.FormulaEvent;
import com.ramussoft.common.journal.BinaryDataInput;
import com.ramussoft.common.journal.BinaryDataOutput;
import com.ramussoft.common.journal.JournaledEngine;

public class FormulaCommand extends Command {

    /**
     *
     */
    private static final long serialVersionUID = 3468778471064301718L;

    private CalculateInfo newInfo;

    private CalculateInfo oldInfo;

    public FormulaCommand(JournaledEngine engine) {
        super(engine);
    }

    public FormulaCommand(JournaledEngine engine, CalculateInfo oldInfo,
                          CalculateInfo newInfo) {
        super(engine);
        this.oldInfo = oldInfo;
        this.newInfo = newInfo;
    }

    @Override
    public void readBody(BinaryDataInput input) throws IOException {
        oldInfo = readInfo(input);
        newInfo = readInfo(input);
    }

    private CalculateInfo readInfo(BinaryDataInput input) throws IOException {
        if (input.readBoolean()) {
            CalculateInfo info = new CalculateInfo();
            info.setAttributeId(input.readLong());
            info.setElementId(input.readLong());
            info.setAutoRecalculate(input.readBoolean());
            info.setFormula(input.readSwimedString());
            return info;
        } else
            return null;
    }

    @Override
    public void redo(IEngine engine) {
        engine.setCalculateInfo(newInfo);
        FormulaEvent event = new FormulaEvent(this.engine, true, oldInfo,
                newInfo);
        this.engine.formulaChanged(event);
    }

    @Override
    public void undo(IEngine engine) {
        CalculateInfo oldInfo = this.oldInfo;
        if (oldInfo == null) {
            oldInfo = new CalculateInfo(newInfo.getElementId(), newInfo
                    .getAttributeId(), null);
            engine.setCalculateInfo(oldInfo);
        } else
            engine.setCalculateInfo(oldInfo);
        FormulaEvent event = new FormulaEvent(this.engine, true, newInfo,
                oldInfo);
        this.engine.formulaChanged(event);
    }

    @Override
    public void writeBody(BinaryDataOutput output) throws IOException {
        writeInfo(oldInfo, output);
        writeInfo(newInfo, output);
    }

    private void writeInfo(CalculateInfo info, BinaryDataOutput output)
            throws IOException {
        if (info != null) {
            output.writeBoolean(true);
            output.writeLong(info.getAttributeId());
            output.writeLong(info.getElementId());
            output.writeBoolean(info.isAutoRecalculate());
            output.writeSwimedString(info.getFormula());
        } else
            output.writeBoolean(false);
    }

}
