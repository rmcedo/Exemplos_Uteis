import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

@Component
public class SqlBuilderWhere {

    private static final String AND = " AND ";

    private static final int ANDSIZE = 5;

    @Inject
    private PessoaRepository pessoaRepository;

    @Inject
    private FamiliaRepository familiaRepository;

    @Inject
    private SKURepository skuRepository;
    
    @Inject OrdemProducaoOrigemRepository opOrigemRepository;

    public String sqlWhereBuilder(RecFilterNeedsOTIF request) {

        var mountSqlWhere = new StringBuilder(" WHERE ");

        mountSqlWhere.append(appendCompanyCode(request.companyCode));
        mountSqlWhere.append(appendBranchCode(request.branchCode, request.companyCode));
        mountSqlWhere.append(appendFamilyCode(request.familyCode));
        mountSqlWhere.append(appendSkuIds(request.skuIds));
        mountSqlWhere.append(appendDocuments(request.documents));
        mountSqlWhere.append(appendStartDate(request.startDate));
        mountSqlWhere.append(appendEndDate(request.endDate));
        mountSqlWhere.append(appendClient(request.client));
        mountSqlWhere.append(appendDemands(request.demands));
        mountSqlWhere.append(appendSituationDemand(request.situationDemand));
        mountSqlWhere.append(appendDeliveryStartDate(request.deliveryStartDate));
        mountSqlWhere.append(appendDeliveryEndDate(request.deliveryEndDate));
        mountSqlWhere.append(appendProductionOrderIds(request.productionOrderIds));


        if (mountSqlWhere.toString().endsWith(AND)) {
            mountSqlWhere.setLength(mountSqlWhere.length() - ANDSIZE);
        }

        return mountSqlWhere.toString();
    }

    private StringBuilder appendCompanyCode(Long companyCode) {
        var sb = new StringBuilder();
        if (companyCode != null) {
            var companyUuid = pessoaRepository.findById(companyCode);
            sb.append("consolidacao_demanda_remote.empresa_id = '").append(companyUuid.getErpxId()).append("'").append(AND);
        }
        return sb;
    }

    private StringBuilder appendBranchCode(Long branchCode, Long companyCode) {
        var sb = new StringBuilder();
        //testar pesquisa
        if (branchCode != null) {
            var filialUuid = pessoaRepository.findFilialById(companyCode, branchCode);
            sb.append("consolidacao_demanda_remote.filial_id = '").append(filialUuid.getErpxId()).append("'").append(AND);
        }
        return sb;
    }

    private StringBuilder appendFamilyCode(String familyCode) {
        var sb = new StringBuilder();
        if (familyCode != null) {
            var familyUuid = familiaRepository.findFamiliaByCodigoThrowsException(familyCode);
            sb.append("e075der.e012fam_id = ").append(familyUuid.getErpxId()).append(AND);
        }
        return sb;
    }

    private StringBuilder appendSkuIds(List<String> skuIds) {
        var sb = new StringBuilder();
        if (skuIds != null && !skuIds.isEmpty()) {
            var skuUuid = new ArrayList<>();
            skuUuid.addAll(skuIds.stream().map(sku ->{  
            return skuRepository.findByCodigoThrowsException(sku);
            }).collect(Collectors.toList()));
            var skuIdsConcat = "'" + String.join("', '", skuIds) + "'";
            sb.append("e120ipd.e075der_id IN (").append(skuIdsConcat).append(")").append(AND);
        }
        return sb;
    }

    private static StringBuilder appendDocuments(List<Long> documents) {
        var sb = new StringBuilder();
        if (documents != null && !documents.isEmpty()) {
            var filteredDocuments = documents.stream()
                    .filter(document -> document.toString().startsWith("PED-"))
                    .collect(Collectors.toList());
            if (!filteredDocuments.isEmpty()) {
                var documentsConcat =String.join("', '", filteredDocuments.toString());
                documentsConcat = documentsConcat.replaceAll("[\\[\\]]", "");
                sb.append("consolidacao_demanda.numero_documento IN (").append(documentsConcat).append(")").append(AND);
            }
        }
        return sb;
    }

    private StringBuilder appendStartDate(LocalDate startDate) {
        var sb = new StringBuilder();
        if (startDate != null) {
            sb.append("consolidacao_demanda_remote.data_geracao >= ").append("'" + startDate + "'").append(AND);
        }
        return sb;
    }

    private StringBuilder appendEndDate(LocalDate endDate) {
        var sb = new StringBuilder();
        if (endDate != null) {
            sb.append("consolidacao_demanda_remote.data_geracao <= ").append("'" + endDate + "'").append(AND);
        }
        return sb;
    }

    private StringBuilder appendClient(List<Long> client) {
        var sb = new StringBuilder();
        if (client != null && !client.isEmpty()) {
            var clientsConcat = String.join("', '", client.toString());
            clientsConcat = clientsConcat.replaceAll("[\\[\\]]", "");
            sb.append("e120ped.e001pescli_id IN (").append(clientsConcat).append(")").append(AND);
        }
        return sb;
    }

    private StringBuilder appendDemands(List<Long> demands) {
        var sb = new StringBuilder();
        if (demands != null && !demands.isEmpty()) {
            var demandsConcat = String.join("', '", demands.toString());
            demandsConcat = demandsConcat.replaceAll("[\\[\\]]", "");
            sb.append("consolidacao_demanda_remote.demanda_codigo IN ( ").append(demandsConcat).append(")").append(AND);
        }
        return sb;
    }

    private StringBuilder appendSituationDemand(EnumSituacaoDemanda situationDemand) {
        var sb = new StringBuilder();
        if (situationDemand != null) {
            sb.append("consolidacao_demanda_remote.situacao = ").append("'" + situationDemand + "'").append(AND);
        }
        return sb;
    }

    private StringBuilder appendDeliveryStartDate(LocalDate deliveryStartDate) {
        var sb = new StringBuilder();
        if (deliveryStartDate != null) {
            sb.append("consolidacao_demanda_remote.data_entrega >= ").append("'" + deliveryStartDate + "'").append(AND);
        }
        return sb;
    }

    private StringBuilder appendDeliveryEndDate(LocalDate deliveryEndDate) {
        var sb = new StringBuilder();
        if (deliveryEndDate != null) {
            sb.append("consolidacao_demanda_remote.data_entrega <= ").append("'" + deliveryEndDate + "'").append(AND);
        }
        return sb;
    }
    
    private StringBuilder appendProductionOrderIds(List<Long> productionOrderIds) {
        var sb = new StringBuilder();
        if (productionOrderIds != null && !productionOrderIds.isEmpty()) {
            var opUUID = new ArrayList<>();
            opUUID.addAll(productionOrderIds.stream().map(op ->{  
            return opOrigemRepository.findByOrdemProducaoId(op);
            }).collect(Collectors.toList()));
            var productionOrderIdsString = String.join("', '", productionOrderIds.toString());
            productionOrderIdsString = productionOrderIdsString.replaceAll("[\\[\\]]", "");
            sb.append("consolidacao_demanda_remote.id_externo_documento IN(").append(productionOrderIdsString).append(")").append(AND);
        }
        return sb;
    }

}
