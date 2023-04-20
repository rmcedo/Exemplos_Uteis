package br.com.senior.erp.man.pcp.programacaocontrole.primitive.otif;

import java.time.LocalDate;
import java.util.List;

import br.com.senior.erpman.pcpprogramacaocontrole.EnumSituacaoDemanda;
import br.com.senior.erpman.pcpprogramacaocontrole.SearchNeedsOTIFInput;

public final class SqlBuilderWhere {

    private static final String AND = " AND ";

    private static final int ANDSIZE = 5;

    private SqlBuilderWhere() {
    }

    public static String sqlWhereBuilder(SearchNeedsOTIFInput request) {

        var mountSqlWhere = new StringBuilder(" WHERE ");

        mountSqlWhere.append(appendProductionOrderIds(request.filterNeedsOTIF.productionOrderIds));
        mountSqlWhere.append(appendCompanycompanyCode(request.filterNeedsOTIF.companyCode));
        mountSqlWhere.append(appendBranchCode(request.filterNeedsOTIF.branchCode));
        mountSqlWhere.append(appendFamilyCode(request.filterNeedsOTIF.familyCode));
        mountSqlWhere.append(appendSkuIds(request.filterNeedsOTIF.skuIds));
        mountSqlWhere.append(appendDocuments(request.filterNeedsOTIF.documents));
        mountSqlWhere.append(appendStartDate(request.filterNeedsOTIF.startDate));
        mountSqlWhere.append(appendEndDate(request.filterNeedsOTIF.endDate));
        mountSqlWhere.append(appendClient(request.filterNeedsOTIF.client));
        mountSqlWhere.append(appendDemands(request.filterNeedsOTIF.demands));
        mountSqlWhere.append(appendSituationDemand(request.filterNeedsOTIF.situationDemand));
        mountSqlWhere.append(appendDeliveryStartDate(request.filterNeedsOTIF.deliveryStartDate));
        mountSqlWhere.append(appendDeliveryEndDate(request.filterNeedsOTIF.deliveryEndDate));

        if (mountSqlWhere.toString().endsWith(AND)) {
            mountSqlWhere.setLength(mountSqlWhere.length() - ANDSIZE);
        }

        return mountSqlWhere.toString();
    }

    private static StringBuilder appendProductionOrderIds(List<Long> productionOrderIds) {
        var sb = new StringBuilder();
        if (productionOrderIds != null && !productionOrderIds.isEmpty()) {
            var productionOrderIdsString = "'" + String.join("', '", productionOrderIds.toString()) + "'";
            sb.append("consolidacao_demanda.id_externo_documento IN(SELECT codigo_documento FROM mpcp_op_origem WHERE op_id IN (").append(productionOrderIdsString).append(")").append(AND);
        }
        return sb;
    }

    private static StringBuilder appendCompanycompanyCode(Long companyCode) {
        var sb = new StringBuilder();
        if (companyCode != null) {
            sb.append("consolidacao_demanda.empresa_id = ").append("'" + companyCode + "'").append(AND);
        }
        return sb;
    }

    private static StringBuilder appendBranchCode(Long branchCode) {
        var sb = new StringBuilder();
        if (branchCode != null) {
            sb.append("consolidacao_demanda.filial_id = ").append("'" + branchCode + "'").append(AND);
        }
        return sb;
    }

    private static StringBuilder appendFamilyCode(String familyCode) {
        var sb = new StringBuilder();
        if (familyCode != null) {
            sb.append("e075der.e012fam_id = ").append("'" + familyCode + "'").append(AND);
        }
        return sb;
    }

    private static StringBuilder appendSkuIds(List<String> skuIds) {
        var sb = new StringBuilder();
        if (skuIds != null && !skuIds.isEmpty()) {
            var skuIdsConcat = "'" + String.join("', '", skuIds) + "'";
            sb.append("e120ipd.e075der_id IN (").append(skuIdsConcat).append(")").append(AND);
        }
        return sb;
    }

    private static StringBuilder appendDocuments(List<Long> documents) {
        var sb = new StringBuilder();
        if (documents != null && !documents.isEmpty()) {
            var documentsConcat = "'" + String.join("', '", documents.toString()) + "'";
            sb.append("mpcp_op_origem.codigo_documento IN (").append(documentsConcat).append(")").append(AND);
        }
        return sb;
    }

    private static StringBuilder appendStartDate(LocalDate startDate) {
        var sb = new StringBuilder();
        if (startDate != null) {
            sb.append("consolidacao_demanda.data_geracao >= ").append("'" + startDate + "'").append(AND);
        }
        return sb;
    }

    private static StringBuilder appendEndDate(LocalDate endDate) {
        var sb = new StringBuilder();
        if (endDate != null) {
            sb.append("consolidacao_demanda.data_geracao <= ").append("'" + endDate + "'").append(AND);
        }
        return sb;
    }

    private static StringBuilder appendClient(List<Long> client) {
        var sb = new StringBuilder();
        if (client != null && !client.isEmpty()) {
            var clientsConcat = "'" + String.join("', '", client.toString()) + "'";
            sb.append("e120ped.e001pescli_id IN (").append(clientsConcat).append(")").append(AND);
        }
        return sb;
    }

    private static StringBuilder appendDemands(List<Long> demands) {
        var sb = new StringBuilder();
        if (demands != null && !demands.isEmpty()) {
            var demandsConcat = "'" + String.join("', '", demands.toString()) + "'";
            sb.append("consolidacao_demanda.demanda_codigo IN ( ").append(demandsConcat).append(")").append(AND);
        }
        return sb;
    }

    private static StringBuilder appendSituationDemand(EnumSituacaoDemanda situationDemand) {
        var sb = new StringBuilder();
        if (situationDemand != null) {
            sb.append("consolidacao_demanda.situacao = ").append("'" + situationDemand + "'").append(AND);
        }
        return sb;
    }

    private static StringBuilder appendDeliveryStartDate(LocalDate deliveryStartDate) {
        var sb = new StringBuilder();
        if (deliveryStartDate != null) {
            sb.append("consolidacao_demanda.data_entrega >= ").append("'" + deliveryStartDate + "'").append(AND);
        }
        return sb;
    }

    private static StringBuilder appendDeliveryEndDate(LocalDate deliveryEndDate) {
        var sb = new StringBuilder();
        if (deliveryEndDate != null) {
            sb.append("consolidacao_demanda.data_entrega <= ").append("'" + deliveryEndDate + "'").append(AND);
        }
        return sb;
    }

}
