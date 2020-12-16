package com.sensetif.sink.model.dashboard;

import com.spicter.curtis.api.association.ManyAssociation;
import com.spicter.curtis.api.common.UseDefaults;
import com.spicter.curtis.api.injection.scope.This;
import com.spicter.curtis.api.mixin.Mixins;
import java.util.List;

@Mixins( UserDashboards.Mixin.class )
public interface UserDashboards
{
    List<Dashboard> dashboards();

    interface State
    {
        @UseDefaults
        ManyAssociation<Dashboard> dashboards();
    }

    class Mixin
        implements UserDashboards
    {
        @This
        private State state;

        @Override
        public List<Dashboard> dashboards()
        {
            return state.dashboards().toList();
        }
    }
}
