@Repository
public class RecursoRepository extends AbstractRepository<Recurso, QRecurso> {

    @Inject
    private TranslationHubApi translationHubApi;

    @Override
    protected QRecurso getEntityPath() {
        return QRecurso.recurso;
    }

    public Recurso findByIdThrowsException(Long id) {
        BooleanExpression predicate = getEntityPath().id.eq(id);
        return findOneThrowsException(predicate);
    }

    public Long getQuantidadeRecursosByCentroCusto(long centroCustoId) {
        return count(getEntityPath().centroCusto.id.eq(centroCustoId).and(getEntityPath().ativo.isTrue()));
    }

    public Recurso findById(Long id) {
        BooleanExpression predicate = getEntityPath().id.eq(id);
        return findOne(predicate);
    }

    public boolean isResourceInInformedCompany(Long resourceId, String resourceCode, Long companyId) {
        return findRecursoByIdOrCodeAndEmpresa(resourceId, resourceCode, companyId) != null;
    }

    public Recurso findRecursoByIdOrCodeAndEmpresa(Long resourceId, String resourceCode, Long companyId) {
        BooleanBuilder predicate = new BooleanBuilder();
        predicate.and(getEntityPath().pessoa.id.eq(companyId));

        if (resourceId != null) {
            predicate.and(getEntityPath().id.eq(resourceId));
        } else {
            predicate.and(getEntityPath().codigo.eq(resourceCode));
        }

        return findOne(predicate);
    }

    public Predicate getPredicatePesquisa(final PesquisarRecursoInput request) {
        final BooleanBuilder predicate = new BooleanBuilder();

        predicate //
                .and(getEntityPath().pessoa.codigo.eq(request.empresaCodigo) //
                        .and(getEntityPath().pessoa.tipo.eq(TipoPessoa.EMPRESA)));

        if (request.filialCodigo != null) {
            predicate//
                    .and(getEntityPath().filial.codigo.eq(request.filialCodigo)//
                            .and(getEntityPath().filial.tipo.eq(TipoPessoa.FILIAL)));
        }
        if (request.filtro != null) {
            predicate.and(getEntityPath().codigo.likeIgnoreCase("%" + request.filtro + "%")//
                    .or(getEntityPath().descricao.likeIgnoreCase("%" + request.filtro + "%")));
        }

        processoIndustrialRecursoSubQuery(request, predicate);
        centroRecursoValidationsAndQueries(request, predicate);

        return predicate;
    }


    private void processoIndustrialRecursoSubQuery(final PesquisarRecursoInput request, final BooleanBuilder predicate) {
        if (request.processoIndustrialCodigo != null) {

            JPAQuery<Long> subQuery = new JPAQuery<>(em);
            subQuery //
                    .select(processoIndustrialRecurso.recursoId.id) //
                    .from(processoIndustrialRecurso) //
                    .where(processoIndustrialRecurso.processoIndId.codigo.toUpperCase()//
                            .eq(request.processoIndustrialCodigo.toUpperCase()));

            predicate.and(getEntityPath().id.in(subQuery.fetch()));

        }
    }
    private void centroRecursoValidationsAndQueries(final PesquisarRecursoInput request, final BooleanBuilder predicate) {
        if (request.centroRecursoId != null) {
            predicate.and(getEntityPath().centroRecurso.id.eq(request.centroRecursoId));
        }
        
        if (request.tipoRecurso != null && !request.tipoRecurso.isEmpty()) {
            
            predicate.and(getEntityPath().centroRecurso.tipoRecurso.in(//
                                                                       request.tipoRecurso.stream()//
                                                                       .map(this::mapToTipoRecurso)//
                                                                       .collect(Collectors.toList())));
        }
    }

    private TipoRecurso mapToTipoRecurso(EnumTipoRecurso enumTipoRecurso) {
        switch (enumTipoRecurso) {
            case Celula:
                return TipoRecurso.CELULA_PRODUCAO;
            case Equipamento:
                return TipoRecurso.EQUIPAMENTO;
            case Terceiro:
                return TipoRecurso.RECURSO_TERCEIRO;
            default:
                throw new ServiceException(ErrorCategory.BAD_REQUEST,//
                                           translationHubApi.getMessage(TranslationConstants.TIPO_RECURSO_INVALIDO));
        }
    }

}