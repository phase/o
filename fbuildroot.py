from fbuild.builders.c import guess_static, guess_shared
from fbuild.builders.java import Builder as JavaBuilder
from fbuild.builders import find_program
from fbuild.record import Record
from fbuild.path import Path
import fbuild.db

from optparse import make_option
import sys

def pre_options(parser):
    group = parser.add_option_group('config options')
    group.add_options((
        make_option('--buildtype', help='The build type',
                    choices=['debug', 'release'], default='debug'),
        make_option('--cc', help='Use the given C compiler'),
        make_option('--use-color', help='Use colored output', action='store_true')
    ))

@fbuild.db.caches
def configure(ctx):
    kw = dict(
        debug=ctx.options.buildtype == 'debug',
        optimize=ctx.options.buildtype == 'release',
        flags=['-fdiagnostics-color'] if ctx.options.use_color else [],
        platform_options=[
            ({'posix'}, {'external_libs': ['m']})
        ]
    )
    static = guess_static(ctx, **kw)
    shared = guess_shared(ctx, **kw)
    java = JavaBuilder(ctx)
    jhome = java.get_java_home()
    if not jhome:
        raise fbuild.ConfigFailed('cannot locate Java home directory')
    jinc = [jhome/'include']
    for d in jinc[0].listdir():
        d = jinc[0]/d
        if d.isdir():
            jinc.append(d)
    return Record(static=static, shared=shared, java=java, jinc=jinc)

def build(ctx):
    rec = configure(ctx)
    static = rec.static
    shared = rec.shared
    java = rec.java
    jinc = rec.jinc
    jc = java.compile('src/xyz/jadonfowler/o/*.java')
    static.build_exe('o2', ['o2.c'])
    static.build_exe('tst', ['o2.c'], macros=['UTEST'])
    shared.build_lib('o2-j', ['o2.c'], macros=['WI'], includes=jinc)
