package com.sensetif.sink.model.dashboard;

import com.sensetif.sink.model.HasName;
import com.sensetif.sink.model.HasOwner;
import com.sensetif.sink.model.datapoint.DataPoint;
import com.spicter.curtis.api.association.ManyAssociation;
import com.spicter.curtis.api.common.UseDefaults;
import com.spicter.curtis.api.identity.HasIdentity;
import com.spicter.curtis.api.injection.scope.This;
import com.spicter.curtis.api.mixin.Mixins;
import java.util.List;

@Mixins( Dashboard.Mixin.class )
public interface Dashboard extends HasName, HasIdentity, HasOwner
{
    List<DataPointView> datapointViews();

    interface State
    {
        @UseDefaults
        ManyAssociation<DataPointView> datapoints();
    }

    abstract class Mixin
        implements Dashboard
    {
        @This
        private State state;

        @Override
        public List<DataPointView> datapointViews()
        {
            return state.datapoints().toList();
        }
    }
}
