package org.benf.cfr.reader.state;

import org.benf.cfr.reader.bytecode.analysis.types.JavaRefTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.util.MapFactory;
import org.benf.cfr.reader.util.SetFactory;
import org.benf.cfr.reader.util.functors.UnaryFunction;

import java.util.Map;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: lee
 * Date: 26/11/2013
 * Time: 17:42
 */
public class LocalClassAwareTypeUsageInformation implements TypeUsageInformation {
    private final TypeUsageInformation delegate;
    private final Map<JavaTypeInstance, String> localTypeNames;
    private final Set<String> usedLocalTypeNames;

    public LocalClassAwareTypeUsageInformation(Set<JavaRefTypeInstance> localClassTypes, TypeUsageInformation delegate) {
        this.delegate = delegate;
        Map<String, Integer> lastClassByName = MapFactory.newLazyMap(new UnaryFunction<String, Integer>() {
            @Override
            public Integer invoke(String arg) {
                return 0;
            }
        });
        localTypeNames = MapFactory.newMap();
        usedLocalTypeNames = SetFactory.newSet();
        for (JavaRefTypeInstance localType : localClassTypes) {
            String name = delegate.generateInnerClassShortName(localType);
            /*
             * But strip all the numerics off the front.
             */
            for (int idx = 0, len = name.length(); idx < len; ++idx) {
                char c = name.charAt(idx);
                if (c >= '0' && c <= '9') continue;
                name = name.substring(idx);
                break;
            }
            int x = lastClassByName.get(name);
            lastClassByName.put(name, x + 1);
            String usedName = name + ((x == 0) ? "" : ("_" + x));
            localTypeNames.put(localType, usedName);
            usedLocalTypeNames.add(usedName);
        }
    }

    @Override
    public Set<JavaRefTypeInstance> getUsedClassTypes() {
        return delegate.getUsedClassTypes();
    }

    @Override
    public String getName(JavaTypeInstance type) {
        String local = localTypeNames.get(type);
        if (local != null) return local;

        String res = delegate.getName(type);
        if (usedLocalTypeNames.contains(res)) {
            return type.getRawName();
        }
        return res;
    }

    @Override
    public String generateInnerClassShortName(JavaRefTypeInstance clazz) {
        return delegate.generateInnerClassShortName(clazz);
    }
}