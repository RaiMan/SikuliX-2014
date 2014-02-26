/*
 * Copyright 2010-2014, Sikuli.org, SikuliX.com
 * Released under the MIT License.
 *
 * modified RaiMan
 */
package org.sikuli.script;

import org.sikuli.basics.Settings;
import org.sikuli.basics.Debug;
import java.awt.AWTException;
import java.util.*;
import org.sikuli.natives.FindInput;
import org.sikuli.natives.FindResult;
import org.sikuli.natives.FindResults;
import org.sikuli.natives.Mat;
import org.sikuli.natives.Vision;

/**
 * INTERNAL USE
 * implements the observe action for a region and calls the ObserverCallBacks
 */
public class Observer {

  protected enum State {
    FIRST, UNKNOWN, MISSING, APPEARED, VANISHED, REPEAT
  }
  private Region _region;
  private Mat _lastImgMat = null;
  private org.opencv.core.Mat _lastImageMat = null;
  private Map<Object, State> _state;
  private Map<Object, Long> _wait;
  private Map<Object, Integer> _count;
  private Map<Object, Match> _lastMatch;
  private Map<Object, String> _names;
//  private Map<Object, SikuliEventObserver> _appearOb, _vanishOb;
  private Map<Object, Object> _appearOb, _vanishOb;
//  private Map<Integer, SikuliEventObserver> _changeOb;
  private Map<Integer, Object> _changeOb;
  private Map<Integer, Integer> _countc;
  private Map<Integer, String> _cnames;
  private int _minChanges;
  private boolean sthgLeft;

  public Observer(Region region) {
    _region = region;
    _state = new HashMap<Object, State>();
    _wait = new HashMap<Object, Long>();
    _count = new HashMap<Object, Integer>();
    _lastMatch = new HashMap<Object, Match>();
    _names = new HashMap<Object, String>();
//    _appearOb = new HashMap<Object, SikuliEventObserver>();
//    _vanishOb = new HashMap<Object, SikuliEventObserver>();
//    _changeOb = new HashMap<Integer, SikuliEventObserver>();
    _appearOb = new HashMap<Object, Object>();
    _vanishOb = new HashMap<Object, Object>();
    _changeOb = new HashMap<Integer, Object>();
    _countc = new HashMap<Integer, Integer>();
    _cnames = new HashMap<Integer, String>();
  }

  public void initialize() {
    Debug.log(2, "SikuliEventManager: resetting observe states for " + _region.toStringShort());
    sthgLeft = true;
    for (Object ptn : _state.keySet()) {
      _state.put(ptn, State.FIRST);
      _count.put(ptn, 0);
    }
    for (int n : _changeOb.keySet()) {
      _countc.put(n, 0);
    }
  }

  public void setRegion(Region reg) {
    _region = reg;
  }

  public int getCount(Object ptn) {
    return _count.get(ptn);
  }

  private <PSC> float getSimiliarity(PSC ptn) {
    float similarity = -1f;
    if (ptn instanceof Pattern) {
      similarity = ((Pattern) ptn).getSimilar();
    }
    if (similarity < 0) {
      similarity = (float) Settings.MinSimilarity;
    }
    return similarity;
  }

  public <PSC> void addAppearObserver(PSC ptn, ObserverCallBack ob, String name) {
    _appearOb.put(ptn, ob);
    _state.put(ptn, State.FIRST);
    _names.put(ptn, name);
  }

  public <PSC> void removeAppearObserver(PSC ptn) {
    Observing.remove(_names.get(ptn));
    _names.remove(ptn);
    _appearOb.remove(ptn);
    _state.remove(ptn);
  }

  public <PSC> void addVanishObserver(PSC ptn, ObserverCallBack ob, String name) {
    _vanishOb.put(ptn, ob);
    _state.put(ptn, State.FIRST);
    _names.put(ptn, name);
  }

  public <PSC> void removeVanishObserver(PSC ptn) {
    Observing.remove(_names.get(ptn));
    _names.remove(ptn);
    _vanishOb.remove(ptn);
    _state.remove(ptn);
  }

  private void callAppearObserver(Object ptn, Match m) {
    ObserveAppear se = new ObserveAppear(ptn, m, _region);
    Object ao = _appearOb.get(ptn);
    Observing.addEvent(_names.get(ptn), se);
    if (ao != null && ao instanceof ObserverCallBack) {
      ((ObserverCallBack)_appearOb.get(ptn)).appeared(se);
    }
  }

  private void callVanishObserver(Object ptn, Match m) {
    ObserveVanish se = new ObserveVanish(ptn, m, _region);
    Object ao = _vanishOb.get(ptn);
    Observing.addEvent(_names.get(ptn), se);
    if (ao != null && ao instanceof ObserverCallBack) {
      ((ObserverCallBack)_vanishOb.get(ptn)).vanished(se);
    }
  }

  private void checkPatterns(ScreenImage simg) {
    Finder finder = null;
    if (Settings.UseImageFinder) {
      finder = new ImageFinder(_region);
      ((ImageFinder) finder).setIsMultiFinder();
    }
    else {
      finder = new Finder(simg, _region);
    }
    String imgOK;
    Debug.log(3, "observe: checkPatterns entry: sthgLeft: %s isObserving: %s", sthgLeft, _region.isObserving());
    for (Object ptn : _state.keySet()) {
      if (_state.get(ptn) != State.FIRST &&
          _state.get(ptn) != State.UNKNOWN &&
          _state.get(ptn) != State.REPEAT) {
        continue;
      }
      imgOK = null;
      if (ptn instanceof String) {
        imgOK = finder.find((String) ptn);
        Image img = Image.create((String) ptn);
        if (img.isValid()) {
          imgOK = finder.find(img);
        } else if (img.isText()){
          imgOK = finder.findText((String) ptn);
        }
      } else if (ptn instanceof Pattern) {
        imgOK = finder.find((Pattern) ptn);
      } else if (ptn instanceof Image) {
        imgOK = finder.find((Image) ptn);
      }
      if (null == imgOK) {
        Debug.error("EventMgr: checkPatterns: Image not valid", ptn);
        _state.put(ptn, State.MISSING);
        continue;
      }
      if (_state.get(ptn) == State.REPEAT) {
        Debug.log(3, "repeat: checking");
        if (_lastMatch.get(ptn).exists(ptn) != null) {
          if ((new Date()).getTime() > _wait.get(ptn)) {
            _state.put(ptn, State.APPEARED);
            Debug.log(3, "repeat: vanish timeout");
            // time out
          } else {
            sthgLeft = true;
          }
          continue; // not vanished within given time or still there
        } else {
          _state.put(ptn, State.UNKNOWN);
          sthgLeft = true;
          Debug.log(3, "repeat: has vanished");
          continue; // has vanished, repeat
        }
      }
      Match m = null;
      boolean hasMatch = false;
      if (finder.hasNext()) {
        m = finder.next();
        if (m.getScore() >= getSimiliarity(ptn)) {
          hasMatch = true;
          _lastMatch.put(ptn, m);
        }
      }
      if (hasMatch) {
        Debug.log(2, "EventMgr: checkPatterns: " + ptn.toString() + " match: " +
                m.toStringShort() + " in " + _region.toStringShort());
      } else if (_state.get(ptn) == State.FIRST) {
        Debug.log(2, "EventMgr: checkPatterns: " + ptn.toString() + " match: " +
                "NO" + " in " + _region.toStringShort());
        _state.put(ptn, State.UNKNOWN);
      }
      if (_appearOb.containsKey(ptn)) {
        if (_state.get(ptn) != State.APPEARED) {
          if (hasMatch) {
            _state.put(ptn, State.APPEARED);
            _count.put(ptn, _count.get(ptn) + 1);
            callAppearObserver(ptn, m);
          } else {
            sthgLeft = true;
          }
        }
      } else if (_vanishOb.containsKey(ptn)) {
        if (_state.get(ptn) != State.VANISHED) {
          if (!hasMatch) {
            _state.put(ptn, State.VANISHED);
            _count.put(ptn, _count.get(ptn) + 1);
            callVanishObserver(ptn, _lastMatch.get(ptn));
          } else {
            sthgLeft = true;
          }
        }
      }
      if (!_region.isObserving()) {
        break;
      }
    }
    Debug.log(3, "observe: checkPatterns exit: sthgLeft: %s isObserving: %s", sthgLeft, _region.isObserving());
  }

  public void repeat(ObserveEvent.Type type, Object pattern, Match match, long secs) {
    if (type == ObserveEvent.Type.CHANGE) {
      Debug.error("EventMgr: repeat: CHANGE repeats automatically");
    } else if (type == ObserveEvent.Type.VANISH) {
      Debug.error("EventMgr: repeat: not supported for VANISH");
    } else if (type == ObserveEvent.Type.APPEAR) {
      _state.put(pattern, State.REPEAT);
      if (secs <= 0) {
        secs = (long) _region.getWaitForVanish();
      }
      _wait.put(pattern, (new Date()).getTime() + 1000 * secs);
      Debug.log(2, "EventMgr: repeat: requested for APPEAR: " +
              pattern.toString() + " at " + match.toStringShort() + " after " + secs + " seconds");
      sthgLeft = true;
    }
  }

  public void addChangeObserver(int threshold, ObserverCallBack ob, String name) {
    _changeOb.put(new Integer(threshold), ob);
    _minChanges = getMinChanges();
    _cnames.put(threshold, name);
  }

  public void removeChangeObserver(int threshold) {
    Observing.remove(_cnames.get(threshold));
    _names.remove(threshold);
    _changeOb.remove(new Integer(threshold));
    _minChanges = getMinChanges();
  }

  private int getMinChanges() {
    int min = Integer.MAX_VALUE;
    for (Integer n : _changeOb.keySet()) {
      if (n < min) {
        min = n;
      }
    }
    return min;
  }

  private void callChangeObserver(FindResults results) throws AWTException {
    for (Integer n : _changeOb.keySet()) {
      List<Match> changes = new ArrayList<Match>();
      for (int i = 0; i < results.size(); i++) {
        FindResult r = results.get(i);
        if (r.getW() * r.getH() >= n) {
          changes.add(_region.toGlobalCoord(new Match(r, _region.getScreen())));
        }
      }
      if (changes.size() > 0) {
        _countc.put(n, _countc.get(n) + 1);
        ObserveChange se = new ObserveChange(changes, _region);
        Object ao = _changeOb.get(n);
        Observing.addEvent(_cnames.get(n), se);
        if (ao instanceof ObserverCallBack) {
          ((ObserverCallBack)_changeOb.get(n)).changed(se);
        }
      }
    }
  }

  private void checkChanges(ScreenImage img) {
    if (Settings.UseImageFinder) {
      //TODO hack to hide the native call - should be at the top
      if (_lastImageMat == null) {
          _lastImageMat = new org.opencv.core.Mat();
      }
      if (_lastImageMat.empty()) {
        _lastImageMat = Image.createMat(img.getImage());
        return;
      }
      ImageFinder f = new ImageFinder(_lastImageMat);
      f.setMinChanges(_minChanges);
      org.opencv.core.Mat current = Image.createMat(img.getImage());
      if (f.hasChanges(current)) {
        //TODO implement ChangeObserver: processing changes
        Debug.log(3, "ChangeObserver: processing changes");
      }
      _lastImageMat = current;
    }
    else {
      if (_lastImgMat == null) {
        _lastImgMat = Image.convertBufferedImageToMat(img.getImage());
        return;
      }
      FindInput fin = new FindInput();
      fin.setSource(_lastImgMat);
      Mat target = Image.convertBufferedImageToMat(img.getImage());
      fin.setTarget(target);
      fin.setSimilarity(_minChanges);
      FindResults results = Vision.findChanges(fin);
      try {
        callChangeObserver(results);
      } catch (AWTException e) {
        Debug.error("EventMgr: checkChanges: ", e.getMessage());
      }
      _lastImgMat = target;
    }
  }

  public boolean update(ScreenImage simg) {
    Debug.log(3, "observe: update entry: sthgLeft: %s obs? %s", sthgLeft, _region.isObserving());
    boolean ret;
    ret = sthgLeft;
    if (sthgLeft) {
      sthgLeft = false;
      checkPatterns(simg);
      if (!_region.isObserving()) {
        return false;
      }
    }
    if (_region.isObserving()) {
      ret = sthgLeft;
      if (_changeOb.size() > 0) {
        checkChanges(simg);
        if (!_region.isObserving()) {
          return false;
        }
        ret = true;
      }
    }
    Debug.log(3, "observe: update exit: ret: %s", ret);
    return ret;
  }
}