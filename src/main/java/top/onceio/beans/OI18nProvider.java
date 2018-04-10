package top.onceio.beans;

import top.onceio.aop.annotation.Cacheable;
import top.onceio.db.dao.DaoHolder;
import top.onceio.db.tbl.OI18n;
import top.onceio.mvc.annocations.AutoApi;

@AutoApi(OI18n.class)
@Cacheable
public class OI18nProvider extends DaoHolder<OI18n> {

}
