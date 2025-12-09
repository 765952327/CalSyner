package com.calsync.sync.radicale;

import com.calsync.sync.Param;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RadicaleParam implements Param {
    private String key;
    private String name;
    
    @Override
    public String getKey() {
        return key;
    }
    
    @Override
    public String getAlias() {
        return name;
    }
}
