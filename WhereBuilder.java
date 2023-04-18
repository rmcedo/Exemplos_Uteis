import javax.inject.Inject;
/**
 * Classe de Exemplo de Montagem de WHERE utilitário
 * 
 * ps.: necessário verificar os ALIAS na hora da criação do SELECT,
 * para que haja concordância com os ALIAS utilizados na classe SqlBuilderUtil
 * 
 * cd = consolidacao_demanda
 * ed = e075der
 * ei = e120ipd
 * ep = e120ped 
 * moo = mpcp_op_origem
 *   
 * @author Rafael.DaSilva
 *
 */
public class SqlBuilderUtil {
    private static final String AND = " AND ";
    
    private static final int ANDSIZE = 5;
    
    @Inject
    private FiltroRecordHandler filterHandler;
    
    
    public static String sqlWhere() {
        
        var stringBuilder  = new StringBuilder( "WHERE ");
        
        appendCompanyId(stringBuilder);
        appendBranchId(stringBuilder);
        appendFamilyId(stringBuilder);
        appendSkuIds(stringBuilder);
        appendDocuments(stringBuilder);
        appendStartDate(stringBuilder);
        appendEndDate(stringBuilder);
        appendClient(stringBuilder);
        appendDemands(stringBuilder);
        appendSituationDemand(stringBuilder);
        appendDeliveryStartDate(stringBuilder);
        appendDeliveryEndDate(stringBuilder);
        
        if (stringBuilder.toString().endsWith(AND)) {
            stringBuilder.setLength(stringBuilder.length() - ANDSIZE);
        }

        return stringBuilder.toString();
    }
    
    private static void appendCompanyId(StringBuilder sb) {
        if(filterHandler.companyId != null) {
            sb.append("cd.empresa_id = ").append("'"+filterHandler.companyId+"'").append(AND);
        }
    }
    private static void appendBranchId(StringBuilder sb) {
        if(filterHandler.branchId != null) {
            sb.append("cd.filial_id = ").append("'"+filterHandler.branchId+"'").append(AND);
        }
    }
    private static void appendFamilyId(StringBuilder sb) {
        if(filterHandler.familyId != null) {
            sb.append("ed.e012fam_id = ").append("'"+filterHandler.familyId+"'").append(AND);
        }
    }
    private static void appendSkuIds(StringBuilder sb) {
        if(filterHandler.skuIds != null && !filterHandler.skuIds.isEmpty()) {
            String skuIds = "'" + String.join("', '", filterHandler.skuIds)+ "'";
            sb.append("ei.e075der_id IN (").append(skuIds).append(")").append(AND);
        }
    }
    private static void appendDocuments(StringBuilder sb) { //RAFAEL AJUSTAR PARA A CONCATENAÇÃO
        if(filterHandler.documents != null && !filterHandler.documentos.isEmpty()) {
            String documents = "'" +- String.join("', '", filterHandler.documents) + "'";
            sb.append("moo.codigo_documento LIKE 'PED-% IN (").append(documents).append(")").append(AND);
        }
    }
    private static void appendStartDate(StringBuilder sb) {
        if(filterHandler.startDate != null) {
            sb.append("cd.data_geracao >= ").append("'"+filter.startDate+"'").append(AND);
        }
    }
    private static void appendEndDate(StringBuilder sb) {
        if(filterHandler.endDate != null) {
            sb.append("cd.data_geracao <= ").append("'".filter.endDate+"'").append(AND);
        }
    }
    private static void appendClient(StringBuilder sb) {
        if(filterHandler.client != null && !filterHandler.client.isEmpty()) {
            String clients = "'" + String.join("', '", filterHandler.client) + "'";
            sb.append("ep.e001pescli_id IN (").append(clients).append(")").append(AND);
        }
        
    }
    private static void appendDemands(StringBuilder sb) {
        if(filterHandler.demands != null && !filterHandler.demands.isEmpty()) {
            String demands = "'" + String.join("', '", filterHandler.demands) + "'";
            sb.append("cd.codigo_documento IN ( ").append(demands).append(")").append(AND);
        }
    }
    private static void appendSituationDemand(StringBuilder sb) {
        if(filterHandler.situationDemand != null) {
            sb.append("cd.situacao = ").append("'"+filterHandler.situationDemand+"'").append(AND);
        }
    }
    private static void appendDeliveryStartDate(StringBuilder sb) {
        if(filterHandler.deliveryStartDate !=null) {
            sb.append("cd.data_entrega >= ").append("'"+filterHandler.deliveryStartDate+"'").append(AND);
        }
    }
    private static void appendDeliveryEndDate(StringBuilder sb) {
        if(filterHandler.deliveryStartDate !=null) {
            sb.append("cd.data_entrega <= ").append("'"+filterHandler.deliveryStartDate+"'").append(AND);
        }
    }

}
