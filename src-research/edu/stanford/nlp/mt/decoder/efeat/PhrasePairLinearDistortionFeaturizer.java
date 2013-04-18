package edu.stanford.nlp.mt.decoder.efeat;

import java.util.*;

import edu.stanford.nlp.mt.base.ConcreteTranslationOption;
import edu.stanford.nlp.mt.base.FeatureValue;
import edu.stanford.nlp.mt.base.Featurizable;
import edu.stanford.nlp.mt.base.Sequence;
import edu.stanford.nlp.mt.base.IString;
import edu.stanford.nlp.mt.decoder.feat.IncrementalFeaturizer;
import edu.stanford.nlp.util.Index;

/**
 * 
 * @author danielcer
 * 
 */
public class PhrasePairLinearDistortionFeaturizer implements
    IncrementalFeaturizer<IString, String> {
  public static final String FEATURE_PREFIX = "PLD";
  public static final String ABSOLUTE = ":a";
  public static final String LEFT_SHIFT = ":l";
  public static final String RIGHT_SHIFT = ":r";
  public static final boolean DEFAULT_USE_LRDISTANCE = true;
  public static final boolean DEFAULT_DO_PRIOR = true;
  public static final String CURRENT = ":c";
  public static final String PRIOR = ":p";

  final boolean doPrior;
  final boolean lrDistance;

  public PhrasePairLinearDistortionFeaturizer() {
    lrDistance = DEFAULT_USE_LRDISTANCE;
    doPrior = DEFAULT_DO_PRIOR;
  }

  public PhrasePairLinearDistortionFeaturizer(String... args) {
    lrDistance = Boolean.parseBoolean(args[0]);
    doPrior = Boolean.parseBoolean(args[1]);
  }

  @Override
  public FeatureValue<String> featurize(Featurizable<IString, String> f) {
    return null;
  }

  @Override
  public void initialize(List<ConcreteTranslationOption<IString,String>> options,
      Sequence<IString> foreign, Index<String> featureIndex) {
  }

  @Override
  public List<FeatureValue<String>> listFeaturize(
      Featurizable<IString, String> f) {
    List<FeatureValue<String>> fValues = new LinkedList<FeatureValue<String>>();

    if (f.linearDistortion == 0)
      return null;
    for (int i = 0; i < 2; i++) {
      if (i == 1 && !doPrior)
        break;

      String phrasePair = (i == 0 ? f.sourcePhrase.toString("_") + "=>"
          + f.targetPhrase.toString("_")
          : f.prior != null ? f.prior.sourcePhrase.toString("_") + "=>"
              + f.prior.targetPhrase.toString("_") : "<s>=><s>");

      int signedLinearDistortion = (f.prior == null ? -f.sourcePosition
          : f.prior.hyp.translationOpt
              .signedLinearDistortion(f.hyp.translationOpt));
      String pType = (i == 0 ? CURRENT : PRIOR);
      if (lrDistance) {
        String type = (signedLinearDistortion < 0 ? LEFT_SHIFT : RIGHT_SHIFT);
        fValues.add(new FeatureValue<String>(FEATURE_PREFIX + type + pType
            + ":" + phrasePair, f.linearDistortion));
      } else {
        fValues.add(new FeatureValue<String>(FEATURE_PREFIX + ABSOLUTE + pType
            + ":" + phrasePair, f.linearDistortion));
      }
    }

    return fValues;
  }

  public void reset() {
  }
}
