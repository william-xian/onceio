package top.onceio.beans;

import top.onceio.cache.annotation.Cacheable;
import top.onceio.db.dao.DaoProvider;
import top.onceio.db.tbl.OI18n;
import top.onceio.mvc.annocations.AutoApi;

@AutoApi(OI18n.class)
@Cacheable
public class OI18nProvider extends DaoProvider<OI18n>{

}
