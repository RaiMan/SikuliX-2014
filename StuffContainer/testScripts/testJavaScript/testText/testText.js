Settings.OcrTextRead = true;
Settings.InfoLogs = false;
Settings.ActionLogs = false;
Debug.on(2)
s = Screen();
r = wait("image", 3);
//println(r.text());
gap = r.left(1).right(1);
imgGap = Pattern(Image(s.capture(gap))).exact();
println(gap);
noGap = false;
start = -1;
wstart = -1;
gstart = -1;
for (i=0; i<r.w; i++) {
	slot = gap.exists(imgGap, 0);
	if (isNull(slot)) {
		if (start < 0) {
			start = gap.x;
			if(wstart < 0) {
				wstart = start;
			}
		}
	} else {
		if (start > -1) {
			chr = Region.create(start, gap.y, gap.x - start, gap.h);
			gstart = gap.x;
			start = -1;
			continue;
		}
	}
	gap.x += 1;
}
SIKULIX.terminate(1, "");
