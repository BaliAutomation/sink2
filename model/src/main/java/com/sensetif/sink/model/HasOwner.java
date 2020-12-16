package com.sensetif.sink.model;

import com.spicter.curtis.api.property.Property;

public interface HasOwner
{
    Property<IsOwner> owner();
}
