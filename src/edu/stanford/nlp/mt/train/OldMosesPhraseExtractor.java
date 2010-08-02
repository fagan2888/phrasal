package edu.stanford.nlp.mt.train;

import java.util.*;


/**
 * Phrase extraction as implemented in Moses, which is not particularly efficient.
 * Time complexity is O(m^2 * s * t), where m is the maximum phrase length, and
 * s and t are respectively the lengths of the source and target sentence.
 * MosesPhraseExtractor offers an implementation that is linear in the
 * maximum phrase length, i.e., O(m * s * t).
 *
 * @author Michel Galley
 * @deprecated Use MosesPhraseExtractor instead (OldMosesPhraseExtractor is slower,
 * but should produce the same output).
 */
@SuppressWarnings("unused")
public class OldMosesPhraseExtractor extends AbstractPhraseExtractor {

  public OldMosesPhraseExtractor(Properties prop, AlignmentTemplates alTemps, List<AbstractFeatureExtractor> extractors) {
    super(prop, alTemps, extractors);
  }

  @Override
  public void extractPhrases(WordAlignment sent) {

    int fsize = sent.f().size();
    int esize = sent.e().size();

    if(fsize > MAX_SENT_LEN || esize > MAX_SENT_LEN) {
      System.err.println("Warning: skipping too long sentence. Length: f="+fsize+" e="+esize);
      return;
    }

    alGrid.init(sent);
    if(fsize < PRINT_GRID_MAX_LEN && esize < PRINT_GRID_MAX_LEN)
      alGrid.printAlTempInGrid("line: "+sent.getId(),null,System.err);

    // Sentence boundaries:
    if(extractBoundaryPhrases) {
      // make sure we can always translate <s> as <s> and </s> as </s>:
      addPhraseToIndex(sent,0,0,0,0,true,1.0f);
      addPhraseToIndex(sent,fsize-1,fsize-1,esize-1,esize-1,true,1.0f);
    }

    // For each English phrase:
    for(int e1=0; e1<esize; ++e1) {
      for(int e2=e1; e2<esize && e2-e1<maxPhraseLenE; ++e2) {
        // Find range of f aligning to e1...e2:
        int f1=Integer.MAX_VALUE;
        int f2=Integer.MIN_VALUE;
        for(int ei=e1; ei<=e2; ++ei) {
          for(int fi : sent.e2f(ei)) {
            if(fi<f1) f1 = fi;
            if(fi>f2) f2 = fi;
          }
        }
        // Phrase too long:
        if(f2-f1>=maxPhraseLenF)
          continue; 
        // No word alignment within that range, or phrase too long?
        if(NO_EMPTY_ALIGNMENT && f1>f2)
          continue;
        // Check if range [e1-e2] [f1-f2] is admissible:
        boolean admissible = true;
        for(int fi=f1; fi<=f2 && admissible; ++fi) {
          for(int ei : sent.f2e(fi)) {
            if(ei<e1 || ei>e2) {
              admissible = false;
              break;
            }
          }
        }
        if(!admissible)
          continue;
        // See how much we can expand the phrase to cover unaligned words:
        int F1 = f1, F2 = f2;
        while(F1-1>=0    && f2-F1<maxPhraseLenF-1 && sent.f2e(F1 - 1).isEmpty()) { --F1; }
        while(F2+1<fsize && F2-f1<maxPhraseLenF-1 && sent.f2e(F2 + 1).isEmpty()) { ++F2; }

        for(int i=F1; i<=f1; ++i)
          for(int j=f2; j<=F2; ++j)
            if(j-i < maxPhraseLenF)
              addPhraseToIndex(sent,i,j,e1,e2,true,1.0f);
      }
    }
    featurize(sent);
  }
}