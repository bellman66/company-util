package youn.project.company.module.file.data;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter(value = AccessLevel.PRIVATE)
public class PortalRes {

    private String bStt;
    private String taxType;
    private String endDt;

    public static PortalRes create(String _bStt, String _taxType, String _endDt) {
        PortalRes result = new PortalRes();
        result.setBStt(_bStt);
        result.setTaxType(_taxType);
        result.setEndDt(_endDt);

        return result;
    }
}
