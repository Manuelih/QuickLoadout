import sys
from PIL import Image, ImageDraw, ImageFont

S = 1024  # drawn at 2x, saved at 512

SWORD = [
    "..........##",
    ".........#cc#",
    "........#ccb#",
    ".......#ccb#.",
    "......#ccb#..",
    ".#...#ccb#...",
    ".##.#ccb#....",
    "..##ccb#.....",
    "..#hh##......",
    ".#hh#.##.....",
    "#hh#...#.....",
    "##...........",
]
PICK = [
    "...#####....",
    "..#ccccc##..",
    ".#cb###bcc#.",
    ".##...#hb#c#",
    "......#hh#c#",
    ".....#hh#.##",
    "....#hh#....",
    "...#hh#.....",
    "..#hh#......",
    ".#hh#.......",
    "#hh#........",
    "##..........",
]
APPLE = [
    ".....##.....",
    "......#.gg..",
    "..###.#gg...",
    ".#rrr##rr#..",
    "#rwrrrrrrr#.",
    "#rwrrrrrrr#.",
    "#rrrrrrrrr#.",
    "#rrrrrrrrd#.",
    ".#rrrrrrd#..",
    ".#rrrrddd#..",
    "..##d##d#...",
    "............",
]
PAL = {
    '#': (24, 28, 36), 'c': (120, 235, 225), 'b': (60, 170, 175), 'h': (150, 100, 55),
    'r': (225, 50, 60), 'w': (255, 170, 170), 'd': (150, 25, 40), 'g': (90, 185, 70),
}


def sprite(d, rows, x0, y0, px):
    for y, row in enumerate(rows):
        for x, ch in enumerate(row):
            if ch in PAL:
                d.rectangle([x0 + x * px, y0 + y * px, x0 + (x + 1) * px - 1, y0 + (y + 1) * px - 1], fill=PAL[ch])


img = Image.new('RGBA', (S, S), (0, 0, 0, 0))
mask = Image.new('L', (S, S), 0)
ImageDraw.Draw(mask).rounded_rectangle([24, 24, S - 24, S - 24], radius=190, fill=255)
bg = Image.new('RGBA', (S, S))
bd = ImageDraw.Draw(bg)
for y in range(S):
    t = y / S
    bd.line([(0, y), (S, y)], fill=(int(40 + 25 * t), int(70 + 20 * t), int(120 - 30 * t), 255))
img.paste(bg, (0, 0), mask)
d = ImageDraw.Draw(img)

# three hotbar slots, the middle one selected
slot, gap = 250, 26
x0 = (S - (3 * slot + 2 * gap)) // 2
y0 = 420
for i, item in enumerate([SWORD, PICK, APPLE]):
    x = x0 + i * (slot + gap)
    d.rectangle([x, y0, x + slot, y0 + slot], fill=(30, 32, 40))
    d.rectangle([x + 12, y0 + 12, x + slot - 12, y0 + slot - 12], fill=(139, 139, 139))
    d.rectangle([x + 12, y0 + 12, x + slot - 12, y0 + 22], fill=(90, 90, 90))
    d.rectangle([x + 12, y0 + 12, x + 22, y0 + slot - 12], fill=(90, 90, 90))
    d.rectangle([x + 12, y0 + slot - 22, x + slot - 12, y0 + slot - 12], fill=(200, 200, 200))
    d.rectangle([x + slot - 22, y0 + 12, x + slot - 12, y0 + slot - 12], fill=(200, 200, 200))
    sprite(d, item, x + 29, y0 + 29, 16)
    if i == 1:
        d.rectangle([x - 14, y0 - 14, x + slot + 14, y0 + slot + 14], outline=(255, 255, 255), width=14)

# "load" arrows above the bar: three chevrons dropping into the slots
for i in range(3):
    cx = x0 + i * (slot + gap) + slot // 2
    for k, a in enumerate((150, 255)):
        top = 150 + k * 95
        d.polygon([(cx - 70, top), (cx + 70, top), (cx, top + 85)], fill=(255, 210, 70, a))

# four loadout pips below
FONT = ImageFont.truetype('C:/Windows/Fonts/arialbd.ttf', 44)
for i in range(4):
    cx = S // 2 + (i - 1.5) * 110
    r = 30
    d.rounded_rectangle([cx - r, 760, cx + r, 760 + 2 * r], radius=12, fill=(255, 210, 70) if i == 0 else (225, 230, 240))
    d.text((cx, 760 + r), str(i + 1), fill=(30, 40, 60), font=FONT, anchor='mm')

out = sys.argv[1] if len(sys.argv) > 1 else 'icon-512.png'
img.resize((512, 512), Image.LANCZOS).save(out)
