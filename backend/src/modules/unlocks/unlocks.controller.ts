import { BadRequestException, Body, Controller, Get, Post, Req, UseGuards } from '@nestjs/common';
import {
  ApiBearerAuth,
  ApiCreatedResponse,
  ApiOkResponse,
  ApiOperation,
  ApiTags,
} from '@nestjs/swagger';
import { PrismaService } from '../../prisma/prisma.service';
import { JwtAuthGuard } from '../auth/jwt-auth.guard';
import { JwtPayload } from '../auth/auth.service';
import {
  DailySummaryResponseDto,
  SyncUnlocksDto,
  SyncUnlocksResponseDto,
} from './sync-unlocks.dto';

@ApiTags('Unlocks')
@ApiBearerAuth('JWT')
@Controller('unlocks')
@UseGuards(JwtAuthGuard)
export class UnlocksController {
  constructor(private readonly prisma: PrismaService) {}

  @Post('sync')
  @ApiOperation({ summary: 'Sync daily unlock summaries (and optional events) to cloud' })
  @ApiCreatedResponse({ type: SyncUnlocksResponseDto })
  async sync(@Req() req: { user: JwtPayload }, @Body() dto: SyncUnlocksDto) {
    const userId = req.user.sub;

    const device = await this.prisma.device.findFirst({
      where: { userId },
      orderBy: { lastSeenAt: 'desc' },
    });
    if (!device) {
      throw new BadRequestException('Device not registered');
    }

    for (const summary of dto.dailySummaries) {
      await this.prisma.dailyUnlockSummary.upsert({
        where: {
          userId_date: {
            userId,
            date: new Date(summary.date),
          },
        },
        create: {
          userId,
          date: new Date(summary.date),
          unlockCount: summary.unlockCount,
          payableAmountP: summary.payableAmountP ?? 0,
          capped: summary.capped ?? false,
        },
        update: {
          unlockCount: summary.unlockCount,
          payableAmountP: summary.payableAmountP ?? 0,
          capped: summary.capped ?? false,
          computedAt: new Date(),
        },
      });
    }

    if (dto.events?.length) {
      for (const event of dto.events) {
        await this.prisma.unlockEvent.upsert({
          where: { id: event.id },
          create: {
            id: event.id,
            userId,
            deviceId: device.id,
            ts: new Date(event.ts),
            source: event.source,
          },
          update: {},
        });
      }
    }

    return { synced: dto.dailySummaries.length, events: dto.events?.length ?? 0 };
  }

  @Get('summary')
  @ApiOperation({ summary: 'Get my daily unlock history (last 30 days)' })
  @ApiOkResponse({ type: [DailySummaryResponseDto] })
  async mySummary(@Req() req: { user: JwtPayload }) {
    const userId = req.user.sub;
    const summaries = await this.prisma.dailyUnlockSummary.findMany({
      where: { userId },
      orderBy: { date: 'desc' },
      take: 30,
    });
    return summaries.map((s) => ({
      date: s.date.toISOString().slice(0, 10),
      unlockCount: s.unlockCount,
      payableAmountP: s.payableAmountP,
      capped: s.capped,
      computedAt: s.computedAt.toISOString(),
    }));
  }
}
